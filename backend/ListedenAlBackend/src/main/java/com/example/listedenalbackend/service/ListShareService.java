package com.example.listedenalbackend.service;

import com.example.listedenalbackend.model.ListShare;
import com.example.listedenalbackend.model.ShoppingList;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.repository.ListShareRepository;
import com.example.listedenalbackend.repository.ShoppingListRepository;
import com.example.listedenalbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ListShareService {

    private final ListShareRepository listShareRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final UserRepository userRepository;

    @Autowired
    public ListShareService(ListShareRepository listShareRepository,
                            ShoppingListRepository shoppingListRepository,
                            UserRepository userRepository) {
        this.listShareRepository = listShareRepository;
        this.shoppingListRepository = shoppingListRepository;
        this.userRepository = userRepository;
    }

    public List<ListShare> getAllListShares() {
        return listShareRepository.findAll();
    }

    public Optional<ListShare> getListShareById(Long id) {
        return listShareRepository.findById(id);
    }

    public List<ListShare> getSharesForList(Long shoppingListId) {
        ShoppingList shoppingList = shoppingListRepository.findById(shoppingListId)
                .orElseThrow(() -> new IllegalArgumentException("ShoppingList not found with id: " + shoppingListId));
        return listShareRepository.findByShoppingList(shoppingList);
    }

    public List<ListShare> getSharesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return listShareRepository.findBySharedUser(user);
    }

    @Transactional
    public ListShare createListShare(Long shoppingListId, Long sharedUserId, ListShare.PermissionLevel permissionLevel, Long currentUserId) {
        ShoppingList shoppingList = shoppingListRepository.findById(shoppingListId)
                .orElseThrow(() -> new IllegalArgumentException("ShoppingList not found with id: " + shoppingListId));

        User sharedUser = userRepository.findById(sharedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Shared user not found with id: " + sharedUserId));

        // Yetki kontrolü: Sadece liste sahibi veya ADMIN yetkisine sahip olan paylaşım ekleyebilir
        if (!hasAdminPermission(shoppingList, currentUserId)) {
            throw new SecurityException("User does not have permission to manage shares for this shopping list.");
        }

        // Kendi kendine paylaşım engelle
        if (shoppingList.getOwner().getId().equals(sharedUserId)) {
            throw new IllegalArgumentException("Owner cannot be shared explicitly. Owner already has ADMIN access.");
        }

        // Zaten bir paylaşım var mı kontrol et
        if (listShareRepository.findByShoppingListAndSharedUser(shoppingList, sharedUser).isPresent()) {
            throw new IllegalArgumentException("List is already shared with this user.");
        }

        ListShare listShare = new ListShare();
        listShare.setShoppingList(shoppingList);
        listShare.setSharedUser(sharedUser);
        listShare.setPermissionLevel(permissionLevel);
        listShare.setSharedAt(LocalDateTime.now());

        return listShareRepository.save(listShare);
    }

    @Transactional
    public ListShare updateListShare(Long listShareId, ListShare.PermissionLevel newPermissionLevel, Long currentUserId) {
        return listShareRepository.findById(listShareId).map(existingShare -> {
            // Yetki kontrolü: Sadece liste sahibi veya ADMIN yetkisine sahip olan güncelleyebilir
            if (!hasAdminPermission(existingShare.getShoppingList(), currentUserId)) {
                throw new SecurityException("User does not have permission to update this list share.");
            }

            existingShare.setPermissionLevel(newPermissionLevel);
            return listShareRepository.save(existingShare);
        }).orElseThrow(() -> new IllegalArgumentException("ListShare not found with id: " + listShareId));
    }

    @Transactional
    public void deleteListShare(Long listShareId, Long currentUserId) {
        ListShare listShare = listShareRepository.findById(listShareId)
                .orElseThrow(() -> new IllegalArgumentException("ListShare not found with id: " + listShareId));

        // Yetki kontrolü: Sadece liste sahibi veya ADMIN yetkisine sahip olan silebilir
        if (!hasAdminPermission(listShare.getShoppingList(), currentUserId)) {
            throw new SecurityException("User does not have permission to delete this list share.");
        }

        // Eğer silinmeye çalışılan paylaşım listenin sahibine aitse engelle
        if (listShare.getShoppingList().getOwner().getId().equals(listShare.getSharedUser().getId())) {
            throw new IllegalArgumentException("Cannot delete the owner's share of the list.");
        }

        listShareRepository.deleteById(listShareId);
    }

    // Yardımcı metot: Kullanıcının belirli bir liste üzerinde ADMIN yetkisi var mı?
    private boolean hasAdminPermission(ShoppingList shoppingList, Long userId) {
        if (shoppingList.getOwner().getId().equals(userId)) {
            return true; // Liste sahibi her zaman ADMIN yetkisine sahiptir
        }
        return listShareRepository.findByShoppingListAndSharedUser(shoppingList, userRepository.findById(userId).orElse(null))
                .map(share -> share.getPermissionLevel() == ListShare.PermissionLevel.ADMIN)
                .orElse(false);
    }
}