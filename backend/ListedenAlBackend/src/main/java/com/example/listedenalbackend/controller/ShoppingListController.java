package com.example.listedenalbackend.controller;

import com.example.listedenalbackend.model.ShoppingList;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.service.ShoppingListService;
import com.example.listedenalbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shoppinglists")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final UserService userService;

    @Autowired
    public ShoppingListController(ShoppingListService shoppingListService, UserService userService) {
        this.shoppingListService = shoppingListService;
        this.userService = userService;
    }

    /**
     * Kimliği doğrulanmış kullanıcının erişebildiği tüm alışveriş listelerini getirir (sahibi olduğu veya paylaşılan).
     * GET /api/shoppinglists
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return Alışveriş listelerinin listesiyle birlikte 200 OK yanıtı.
     * @throws RuntimeException Kullanıcı bulunamazsa (GlobalExceptionHandler yakalar).
     */
    @GetMapping
    public ResponseEntity<List<ShoppingList>> getUserAccessibleShoppingLists(@AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        List<ShoppingList> lists = shoppingListService.getUserAccessibleShoppingLists(currentUser.getId());
        return ResponseEntity.ok(lists);
    }

    /**
     * Belirli bir alışveriş listesini ID'sine göre getirir.
     * GET /api/shoppinglists/{id}
     * @param id Alışveriş listesinin ID'si.
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return Alışveriş listesi nesnesiyle birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Liste bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Kullanıcının listeyi görüntüleme yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShoppingList> getShoppingListById(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        // Servis katmanında yetki kontrolü yapılır.
        // shoppingListService.getShoppingListById() direkt çağrılabilir
        // Ancak bu metodun filtreleme kısmı, yetki kontrolünü Controller'da yapıyormuş gibi görünüyor.
        // En doğrusu, getShoppingListById servisine de currentUserId parametresini ekleyip yetkiyi orada kontrol etmek
        // veya basitçe servis katmanında döndürülen listeye erişim hakkını kontrol etmek.
        return shoppingListService.getShoppingListById(id)
                .filter(list -> list.getOwner().getId().equals(currentUser.getId()) ||
                        shoppingListService.getUserAccessibleShoppingLists(currentUser.getId()).contains(list))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("ShoppingList not found or you don't have access to it."));
    }

    /**
     * Yeni bir alışveriş listesi oluşturur.
     * POST /api/shoppinglists
     * @param shoppingList Oluşturulacak alışveriş listesi bilgileri.
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return Oluşturulan alışveriş listesiyle birlikte 201 Created yanıtı.
     * @throws IllegalArgumentException Geçersiz veri varsa (GlobalExceptionHandler yakalar).
     */
    @PostMapping
    public ResponseEntity<ShoppingList> createShoppingList(@RequestBody ShoppingList shoppingList, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        ShoppingList createdList = shoppingListService.createShoppingList(shoppingList, currentUser.getId());
        return new ResponseEntity<>(createdList, HttpStatus.CREATED);
    }

    /**
     * Belirli bir alışveriş listesini günceller.
     * PUT /api/shoppinglists/{id}
     * @param id Güncellenecek alışveriş listesinin ID'si.
     * @param shoppingListDetails Güncellenmiş alışveriş listesi bilgileri.
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return Güncellenmiş alışveriş listesiyle birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Liste bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Güncelleme yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShoppingList> updateShoppingList(@PathVariable Long id, @RequestBody ShoppingList shoppingListDetails, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        ShoppingList updatedList = shoppingListService.updateShoppingList(id, shoppingListDetails, currentUser.getId());
        return ResponseEntity.ok(updatedList);
    }

    /**
     * Belirli bir alışveriş listesini siler.
     * DELETE /api/shoppinglists/{id}
     * @param id Silinecek alışveriş listesinin ID'si.
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return 204 No Content yanıtı.
     * @throws IllegalArgumentException Liste bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Silme yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // HTTP 204 No Content döner
    public void deleteShoppingList(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        shoppingListService.deleteShoppingList(id, currentUser.getId());
    }
}