package com.example.listedenalbackend.controller;

import com.example.listedenalbackend.model.ListShare;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.service.ListShareService;
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
@RequestMapping("/api/shoppinglists/{shoppingListId}/shares")
public class ListShareController {

    private final ListShareService listShareService;
    private final UserService userService; // Sadece current user objesini almak için

    @Autowired
    public ListShareController(ListShareService listShareService, UserService userService) {
        this.listShareService = listShareService;
        this.userService = userService;
    }

    /**
     * Belirli bir alışveriş listesi için tüm paylaşımları getirir.
     * GET /api/shoppinglists/{shoppingListId}/shares
     * @param shoppingListId Paylaşım detayları alınacak alışveriş listesinin ID'si.
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return Liste paylaşımlarının listesiyle birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Liste bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Yetkisiz erişim varsa (GlobalExceptionHandler yakalar).
     */
    @GetMapping
    public ResponseEntity<List<ListShare>> getSharesForList(@PathVariable Long shoppingListId, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        // Yetki kontrolü servis katmanında yapılacak.
        List<ListShare> shares = listShareService.getSharesForList(shoppingListId);
        return ResponseEntity.ok(shares);
    }

    /**
     * Bir alışveriş listesini başka bir kullanıcıyla paylaşır.
     * POST /api/shoppinglists/{shoppingListId}/shares
     * @param shoppingListId Paylaşılacak alışveriş listesinin ID'si.
     * @param requestBody Paylaşılacak kullanıcının ID'sini (sharedUserId) ve yetki seviyesini (permissionLevel) içeren Map.
     * @param currentUserDetails Paylaşımı yapan kullanıcının güvenlik detayları.
     * @return Oluşturulan ListShare nesnesiyle birlikte 201 Created yanıtı.
     * @throws IllegalArgumentException Geçersiz ID, zaten paylaşılmışsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Paylaşım yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @PostMapping
    public ResponseEntity<ListShare> createListShare(@PathVariable Long shoppingListId, @RequestBody Map<String, Object> requestBody, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        Long sharedUserId = Long.valueOf(requestBody.get("sharedUserId").toString());
        ListShare.PermissionLevel permissionLevel = ListShare.PermissionLevel.valueOf(requestBody.get("permissionLevel").toString());

        ListShare createdShare = listShareService.createListShare(shoppingListId, sharedUserId, permissionLevel, currentUser.getId());
        return new ResponseEntity<>(createdShare, HttpStatus.CREATED);
    }

    /**
     * Mevcut bir liste paylaşımının yetki seviyesini günceller.
     * PUT /api/shoppinglists/{shoppingListId}/shares/{listShareId}
     * @param shoppingListId Alışveriş listesinin ID'si (URL anlamsal bütünlüğü için).
     * @param listShareId Güncellenecek ListShare kaydının ID'si.
     * @param requestBody Yeni yetki seviyesini (permissionLevel) içeren Map.
     * @param currentUserDetails İşlemi yapan kullanıcının güvenlik detayları.
     * @return Güncellenmiş ListShare nesnesiyle birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Paylaşım bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Güncelleme yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @PutMapping("/{listShareId}")
    public ResponseEntity<ListShare> updateListShare(@PathVariable Long shoppingListId, @PathVariable Long listShareId, @RequestBody Map<String, String> requestBody, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        ListShare.PermissionLevel newPermissionLevel = ListShare.PermissionLevel.valueOf(requestBody.get("permissionLevel"));

        ListShare updatedShare = listShareService.updateListShare(listShareId, newPermissionLevel, currentUser.getId());
        return ResponseEntity.ok(updatedShare);
    }

    /**
     * Bir liste paylaşımını siler.
     * DELETE /api/shoppinglists/{shoppingListId}/shares/{listShareId}
     * @param shoppingListId Alışveriş listesinin ID'si (URL anlamsal bütünlüğü için).
     * @param listShareId Silinecek ListShare kaydının ID'si.
     * @param currentUserDetails İşlemi yapan kullanıcının güvenlik detayları.
     * @return 204 No Content yanıtı.
     * @throws IllegalArgumentException Paylaşım bulunamazsa veya sahibin paylaşımı silinmeye çalışılırsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Silme yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @DeleteMapping("/{listShareId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteListShare(@PathVariable Long shoppingListId, @PathVariable Long listShareId, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        listShareService.deleteListShare(listShareId, currentUser.getId());
    }
}