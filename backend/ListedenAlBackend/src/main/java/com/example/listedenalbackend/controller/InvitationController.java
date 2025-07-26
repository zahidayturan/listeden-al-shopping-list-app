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
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return Bekleyen davetiyelerin listesiyle birlikte 200 OK yanıtı.
     * @throws RuntimeException Kimliği doğrulanmış kullanıcı bulunamazsa.
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
     * @param requestBody shoppingListId, recipientEmail ve isteğe bağlı olarak permissionLevel içerir.
     * @param currentUserDetails Davetiyeyi gönderen kullanıcının güvenlik detayları.
     * @return Oluşturulan davetiye ile birlikte 201 Created yanıtı.
     * @throws IllegalArgumentException Geçersiz ID, e-posta veya yetki sorunu varsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Davetiye gönderme yetkisi yoksa (GlobalExceptionHandler yakalar).
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
     * @param invitationCode Kabul edilecek davetiyenin benzersiz kodu.
     * @param currentUserDetails Davetiyeyi kabul eden kullanıcının güvenlik detayları.
     * @return Kabul edilen davetiye ile birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Geçersiz kod, süresi dolmuş veya zaten işlenmiş bir davetiye ise (GlobalExceptionHandler yakalar).
     * @throws SecurityException Davetiye bu kullanıcı için değilse (GlobalExceptionHandler yakalar).
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
     * @param invitationCode Reddedilecek davetiyenin benzersiz kodu.
     * @param currentUserDetails Davetiyeyi reddeden kullanıcının güvenlik detayları.
     * @return Reddedilen davetiye ile birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Geçersiz kod veya zaten işlenmiş bir davetiye ise (GlobalExceptionHandler yakalar).
     * @throws SecurityException Davetiye bu kullanıcı için değilse (GlobalExceptionHandler yakalar).
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
     * @param id Silinecek davetiyenin ID'si.
     * @param currentUserDetails İşlemi yapan kullanıcının güvenlik detayları.
     * @return Başarılı silme durumunda 204 No Content yanıtı.
     * @throws IllegalArgumentException Davetiye bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Kullanıcının silme yetkisi yoksa (GlobalExceptionHandler yakalar).
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