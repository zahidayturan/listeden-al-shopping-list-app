package com.example.listedenalbackend.service;

import com.example.listedenalbackend.model.ShoppingList;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.model.ListShare;
import com.example.listedenalbackend.repository.ShoppingListRepository;
import com.example.listedenalbackend.repository.ListShareRepository;
import com.example.listedenalbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ListShareRepository listShareRepository;
    private final UserRepository userRepository; // Paylaşılan kullanıcıları bulmak için

    @Autowired
    public ShoppingListService(ShoppingListRepository shoppingListRepository,
                               ListShareRepository listShareRepository,
                               UserRepository userRepository) {
        this.shoppingListRepository = shoppingListRepository;
        this.listShareRepository = listShareRepository;
        this.userRepository = userRepository;
    }

    public List<ShoppingList> getAllShoppingLists() {
        return shoppingListRepository.findAll();
    }

    public Optional<ShoppingList> getShoppingListById(Long id) {
        return shoppingListRepository.findById(id);
    }

    @Transactional
    public ShoppingList createShoppingList(ShoppingList shoppingList, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner user not found with id: " + ownerId));

        shoppingList.setOwner(owner);
        shoppingList.setCreatedAt(LocalDateTime.now());
        shoppingList.setUpdatedAt(LocalDateTime.now());
        shoppingList.setArchived(false); // Varsayılan olarak arşivlenmemiş

        ShoppingList savedList = shoppingListRepository.save(shoppingList);

        // Listeyi oluşturan kullanıcıya ADMIN yetkisiyle paylaşım kaydı ekle
        ListShare ownerShare = new ListShare();
        ownerShare.setShoppingList(savedList);
        ownerShare.setSharedUser(owner);
        ownerShare.setPermissionLevel(ListShare.PermissionLevel.ADMIN);
        listShareRepository.save(ownerShare);

        return savedList;
    }


    @Transactional
    public ShoppingList updateShoppingList(Long id, ShoppingList updatedListDetails, Long currentUserId) {
        return shoppingListRepository.findById(id).map(existingList -> {
            // Yetki kontrolü: Sadece liste sahibi veya ADMIN yetkisine sahip olan güncelleyebilir
            boolean hasPermission = existingList.getOwner().getId().equals(currentUserId) ||
                    listShareRepository.findByShoppingListAndSharedUser(existingList, userRepository.findById(currentUserId).orElse(null))
                            .map(ls -> ls.getPermissionLevel() == ListShare.PermissionLevel.ADMIN)
                            .orElse(false);

            if (!hasPermission) {
                throw new SecurityException("User does not have permission to update this shopping list.");
            }

            if (updatedListDetails.getName() != null) {
                existingList.setName(updatedListDetails.getName());
            }
            if (updatedListDetails.getDescription() != null) {
                existingList.setDescription(updatedListDetails.getDescription());
            }
            existingList.setArchived(updatedListDetails.isArchived());
            existingList.setUpdatedAt(LocalDateTime.now());
            return shoppingListRepository.save(existingList);
        }).orElseThrow(() -> new IllegalArgumentException("ShoppingList not found with id: " + id));
    }

    @Transactional
    public void deleteShoppingList(Long id, Long currentUserId) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ShoppingList not found with id: " + id));

        // Yetki kontrolü: Sadece liste sahibi veya ADMIN yetkisine sahip olan silebilir
        boolean hasPermission = shoppingList.getOwner().getId().equals(currentUserId) ||
                listShareRepository.findByShoppingListAndSharedUser(shoppingList, userRepository.findById(currentUserId).orElse(null))
                        .map(ls -> ls.getPermissionLevel() == ListShare.PermissionLevel.ADMIN)
                        .orElse(false);

        if (!hasPermission) {
            throw new SecurityException("User does not have permission to delete this shopping list.");
        }

        shoppingListRepository.deleteById(id);
    }

    public List<ShoppingList> getShoppingListsByOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner user not found with id: " + ownerId));
        return shoppingListRepository.findByOwner(owner);
    }

    // Bir kullanıcının hem sahibi olduğu hem de kendisine paylaşılan tüm listeleri getir
    public List<ShoppingList> getUserAccessibleShoppingLists(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Sahip olunan listeler
        List<ShoppingList> ownedLists = shoppingListRepository.findByOwner(user);

        // Paylaşılan listeler
        List<ShoppingList> sharedLists = listShareRepository.findBySharedUser(user)
                .stream()
                .map(ListShare::getShoppingList)
                .collect(Collectors.toList());

        // İki listeyi birleştir ve benzersiz listeleri döndür
        return java.util.stream.Stream.concat(ownedLists.stream(), sharedLists.stream())
                .distinct()
                .collect(Collectors.toList());
    }
}