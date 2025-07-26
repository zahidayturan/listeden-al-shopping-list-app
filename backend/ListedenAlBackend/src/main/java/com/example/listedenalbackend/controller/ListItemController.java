package com.example.listedenalbackend.controller;

import com.example.listedenalbackend.model.ListItem;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.service.ListItemService;
import com.example.listedenalbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shoppinglists/{shoppingListId}/items")
public class ListItemController {

    private final ListItemService listItemService;
    private final UserService userService;

    @Autowired
    public ListItemController(ListItemService listItemService, UserService userService) {
        this.listItemService = listItemService;
        this.userService = userService;
    }

    /**
     * Belirli bir alışveriş listesindeki tüm ürünleri getirir.
     * GET /api/shoppinglists/{shoppingListId}/items
     * @param shoppingListId Alışveriş listesinin ID'si.
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return Ürünlerin listesiyle birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Liste bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Listeyi görüntüleme yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @GetMapping
    public ResponseEntity<List<ListItem>> getItemsByShoppingList(@PathVariable Long shoppingListId, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        List<ListItem> items = listItemService.getItemsByShoppingList(shoppingListId, currentUser.getId());
        return ResponseEntity.ok(items);
    }

    /**
     * Belirli bir alışveriş listesinden tek bir ürünü ID'sine göre getirir.
     * GET /api/shoppinglists/{shoppingListId}/items/{itemId}
     * @param shoppingListId Alışveriş listesinin ID'si (URL anlamsal bütünlüğü için).
     * @param itemId Ürünün ID'si.
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return Ürün nesnesiyle birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Ürün veya liste bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Listeyi görüntüleme yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ListItem> getListItemById(@PathVariable Long shoppingListId, @PathVariable Long itemId, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        // Servis katmanında yetki kontrolü yapılacak.
        // Ayrıca itemId'nin gerçekten shoppingListId'ye ait olduğu kontrolü de servis katmanında yapılabilir.
        ListItem item = listItemService.getListItemById(itemId)
                .filter(li -> li.getShoppingList().getId().equals(shoppingListId))
                .orElseThrow(() -> new IllegalArgumentException("List item not found or does not belong to shopping list " + shoppingListId));

        // Ek yetki kontrolü: Kullanıcının bu listeye erişimi var mı?
        // Bu kontrol zaten listItemService.getItemsByShoppingList() içinde yapılıyor,
        // ancak tekil bir öğeyi doğrudan getirdiğimizde de yapılması mantıklı.
        // Servis katmanında bu kontrolü yapacak bir metot eklemek daha temiz olurdu.
        listItemService.getItemsByShoppingList(shoppingListId, currentUser.getId()); // Sadece yetki kontrolü için çağrılıyor.

        return ResponseEntity.ok(item);
    }


    /**
     * Belirli bir alışveriş listesine yeni bir ürün ekler.
     * POST /api/shoppinglists/{shoppingListId}/items
     * @param shoppingListId Ürünün ekleneceği alışveriş listesinin ID'si.
     * @param listItem Eklenecek ürün bilgileri.
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return Oluşturulan ürün nesnesiyle birlikte 201 Created yanıtı.
     * @throws IllegalArgumentException Liste bulunamazsa veya geçersiz ürün bilgisi varsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Ürün ekleme yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @PostMapping
    public ResponseEntity<ListItem> addListItem(@PathVariable Long shoppingListId, @RequestBody ListItem listItem, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        ListItem createdItem = listItemService.addListItem(shoppingListId, listItem, currentUser.getId());
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    /**
     * Belirli bir ürünü günceller.
     * PUT /api/shoppinglists/{shoppingListId}/items/{itemId}
     * @param shoppingListId Alışveriş listesinin ID'si (URL anlamsal bütünlüğü için).
     * @param itemId Güncellenecek ürünün ID'si.
     * @param listItemDetails Güncellenmiş ürün bilgileri.
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return Güncellenmiş ürün nesnesiyle birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Ürün bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Güncelleme yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<ListItem> updateListItem(@PathVariable Long shoppingListId, @PathVariable Long itemId, @RequestBody ListItem listItemDetails, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        ListItem updatedItem = listItemService.updateListItem(itemId, listItemDetails, currentUser.getId());
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * Belirli bir ürünü siler.
     * DELETE /api/shoppinglists/{shoppingListId}/items/{itemId}
     * @param shoppingListId Alışveriş listesinin ID'si (URL anlamsal bütünlüğü için).
     * @param itemId Silinecek ürünün ID'si.
     * @param currentUserDetails Mevcut oturum açmış kullanıcının güvenlik detayları.
     * @return 204 No Content yanıtı.
     * @throws IllegalArgumentException Ürün bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Silme yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteListItem(@PathVariable Long shoppingListId, @PathVariable Long itemId, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userService.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        listItemService.deleteListItem(itemId, currentUser.getId());
    }
}