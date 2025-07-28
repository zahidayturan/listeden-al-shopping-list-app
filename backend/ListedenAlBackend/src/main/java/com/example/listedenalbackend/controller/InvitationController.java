package com.example.listedenalbackend.controller;

import com.example.listedenalbackend.model.Invitation;
import com.example.listedenalbackend.model.ListShare;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.service.InvitationService;
import com.example.listedenalbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;

    @Autowired
    public InvitationController(InvitationService invitationService, UserService userService) {
        this.invitationService = invitationService;
        this.userService = userService;
    }

    /**
     * Kimliği doğrulanmış kullanıcının bekleyen tüm davetiyelerini getirir.
     * GET /api/invitations/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Invitation>> getPendingInvitationsForUser(@AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found.")); // Bu hata GlobalExceptionHandler tarafından yakalanacak.

        List<Invitation> invitations = invitationService.getPendingInvitationsForUser(currentUser.getId());
        return ResponseEntity.ok(invitations);
    }

    /**
     * Belirli bir alışveriş listesi için başka bir kullanıcıya davetiye gönderir.
     * POST /api/invitations/send
     */
    @PostMapping("/send")
    public ResponseEntity<Invitation> sendInvitation(@RequestBody Map<String, String> requestBody, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User sender = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Long shoppingListId = Long.valueOf(requestBody.get("shoppingListId"));
        String recipientEmail = requestBody.get("recipientEmail");

        // Varsayılan yetki seviyesi EDITOR, istek gövdesinden de okunabilir.
        ListShare.PermissionLevel permissionLevel = ListShare.PermissionLevel.EDITOR;
        if (requestBody.containsKey("permissionLevel")) {
            permissionLevel = ListShare.PermissionLevel.valueOf(requestBody.get("permissionLevel"));
        }

        Invitation newInvitation = invitationService.createInvitation(shoppingListId, recipientEmail, sender.getId(), permissionLevel);
        return new ResponseEntity<>(newInvitation, HttpStatus.CREATED);
    }

    /**
     * Belirli bir davet kodunu kullanarak bir davetiyeyi kabul eder.
     * POST /api/invitations/accept/{invitationCode}
     */
    @PostMapping("/accept/{invitationCode}")
    public ResponseEntity<Invitation> acceptInvitation(@PathVariable String invitationCode, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User acceptingUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Invitation acceptedInvitation = invitationService.acceptInvitation(invitationCode, acceptingUser.getId());
        return ResponseEntity.ok(acceptedInvitation);
    }

    /**
     * Belirli bir davet kodunu kullanarak bir davetiyeyi reddeder.
     * POST /api/invitations/reject/{invitationCode}
     */
    @PostMapping("/reject/{invitationCode}")
    public ResponseEntity<Invitation> rejectInvitation(@PathVariable String invitationCode, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User rejectingUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Invitation rejectedInvitation = invitationService.rejectInvitation(invitationCode, rejectingUser.getId());
        return ResponseEntity.ok(rejectedInvitation);
    }

    /**
     * Belirli bir davetiyeyi siler. Yalnızca davetiyeyi gönderen veya liste yöneticisi silebilir.
     * DELETE /api/invitations/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // HTTP 204 No Content döner
    public void deleteInvitation(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        invitationService.deleteInvitation(id, currentUser.getId());
        // 204 No Content ResponseStatus anotasyonu sayesinde otomatik olarak ayarlanır.
    }
}