package com.example.listedenalbackend.service;

import com.example.listedenalbackend.model.ListItem;
import com.example.listedenalbackend.model.ShoppingList;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.model.ListShare;
import com.example.listedenalbackend.repository.ListItemRepository;
import com.example.listedenalbackend.repository.ShoppingListRepository;
import com.example.listedenalbackend.repository.UserRepository;
import com.example.listedenalbackend.repository.ListShareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ListItemService {

    private final ListItemRepository listItemRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final UserRepository userRepository;
    private final ListShareRepository listShareRepository;

    @Autowired
    public ListItemService(ListItemRepository listItemRepository,
                           ShoppingListRepository shoppingListRepository,
                           UserRepository userRepository,
                           ListShareRepository listShareRepository) {
        this.listItemRepository = listItemRepository;
        this.shoppingListRepository = shoppingListRepository;
        this.userRepository = userRepository;
        this.listShareRepository = listShareRepository;
    }

    public List<ListItem> getAllListItems() {
        return listItemRepository.findAll();
    }

    public Optional<ListItem> getListItemById(Long id) {
        return listItemRepository.findById(id);
    }

    public List<ListItem> getItemsByShoppingList(Long shoppingListId, Long currentUserId) {
        ShoppingList shoppingList = shoppingListRepository.findById(shoppingListId)
                .orElseThrow(() -> new IllegalArgumentException("ShoppingList not found with id: " + shoppingListId));

        // Yetki kontrolü: Kullanıcı listeye erişebilir mi?
        if (!hasViewPermission(shoppingList, currentUserId)) {
            throw new SecurityException("User does not have permission to view this shopping list.");
        }

        return listItemRepository.findByShoppingList(shoppingList);
    }

    @Transactional
    public ListItem addListItem(Long shoppingListId, ListItem listItem, Long currentUserId) {
        ShoppingList shoppingList = shoppingListRepository.findById(shoppingListId)
                .orElseThrow(() -> new IllegalArgumentException("ShoppingList not found with id: " + shoppingListId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found with id: " + currentUserId));

        // Yetki kontrolü: Kullanıcı liste üzerinde EDIT veya ADMIN yetkisine sahip mi?
        if (!hasEditPermission(shoppingList, currentUserId)) {
            throw new SecurityException("User does not have permission to add items to this shopping list.");
        }

        listItem.setShoppingList(shoppingList);
        listItem.setAddedBy(currentUser); // Öğeyi ekleyen kullanıcı
        listItem.setPurchasedBy(null); // Başlangıçta satın alınmadı
        listItem.setCreatedAt(LocalDateTime.now());
        listItem.setUpdatedAt(LocalDateTime.now());
        listItem.setPurchased(false); // Varsayılan olarak satın alınmadı

        return listItemRepository.save(listItem);
    }

    @Transactional
    public ListItem updateListItem(Long id, ListItem updatedItemDetails, Long currentUserId) {
        return listItemRepository.findById(id).map(existingItem -> {
            // Yetki kontrolü: Kullanıcı liste üzerinde EDIT veya ADMIN yetkisine sahip mi?
            if (!hasEditPermission(existingItem.getShoppingList(), currentUserId)) {
                throw new SecurityException("User does not have permission to update this list item.");
            }

            if (updatedItemDetails.getProductName() != null) {
                existingItem.setProductName(updatedItemDetails.getProductName());
            }
            if (updatedItemDetails.getQuantity() != null) {
                existingItem.setQuantity(updatedItemDetails.getQuantity());
            }
            if (updatedItemDetails.getUnit() != null) {
                existingItem.setUnit(updatedItemDetails.getUnit());
            }
            if (updatedItemDetails.getNotes() != null) {
                existingItem.setNotes(updatedItemDetails.getNotes());
            }
            if (updatedItemDetails.getPriority() != null) {
                existingItem.setPriority(updatedItemDetails.getPriority());
            }
            // isPurchased'i buradan doğrudan güncelleyebiliriz veya ayrı bir metot olabilir
            if (updatedItemDetails.isPurchased() != existingItem.isPurchased()) {
                existingItem.setPurchased(updatedItemDetails.isPurchased());
                if (updatedItemDetails.isPurchased()) {
                    existingItem.setPurchasedBy(userRepository.findById(currentUserId).orElse(null));
                } else {
                    existingItem.setPurchasedBy(null); // Satın alma durumu geri alınırsa
                }
            }
            existingItem.setUpdatedAt(LocalDateTime.now());

            return listItemRepository.save(existingItem);
        }).orElseThrow(() -> new IllegalArgumentException("ListItem not found with id: " + id));
    }

    @Transactional
    public void deleteListItem(Long id, Long currentUserId) {
        ListItem listItem = listItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ListItem not found with id: " + id));

        // Yetki kontrolü: Kullanıcı liste üzerinde EDIT veya ADMIN yetkisine sahip mi?
        if (!hasEditPermission(listItem.getShoppingList(), currentUserId)) {
            throw new SecurityException("User does not have permission to delete this list item.");
        }

        listItemRepository.deleteById(id);
    }

    // Yardımcı metotlar for yetki kontrolü
    private boolean hasViewPermission(ShoppingList shoppingList, Long userId) {
        if (shoppingList.getOwner().getId().equals(userId)) {
            return true; // Liste sahibi
        }
        return listShareRepository.findByShoppingListAndSharedUser(shoppingList, userRepository.findById(userId).orElse(null))
                .map(share -> share.getPermissionLevel() == ListShare.PermissionLevel.VIEWER ||
                        share.getPermissionLevel() == ListShare.PermissionLevel.EDITOR ||
                        share.getPermissionLevel() == ListShare.PermissionLevel.ADMIN)
                .orElse(false);
    }

    private boolean hasEditPermission(ShoppingList shoppingList, Long userId) {
        if (shoppingList.getOwner().getId().equals(userId)) {
            return true; // Liste sahibi (her zaman tam yetkiye sahip)
        }
        return listShareRepository.findByShoppingListAndSharedUser(shoppingList, userRepository.findById(userId).orElse(null))
                .map(share -> share.getPermissionLevel() == ListShare.PermissionLevel.EDITOR ||
                        share.getPermissionLevel() == ListShare.PermissionLevel.ADMIN)
                .orElse(false);
    }
}