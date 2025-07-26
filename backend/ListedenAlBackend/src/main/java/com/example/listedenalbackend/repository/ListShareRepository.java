package com.example.listedenalbackend.repository;

import com.example.listedenalbackend.model.ListShare;
import com.example.listedenalbackend.model.ShoppingList;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.model.ListShare.PermissionLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListShareRepository extends JpaRepository<ListShare, Long> {

    // Belirli bir liste ve kullanıcı için paylaşım kaydını bulma
    Optional<ListShare> findByShoppingListAndSharedUser(ShoppingList shoppingList, User sharedUser);

    // Belirli bir kullanıcının paylaşılan tüm listelerini bulma
    List<ListShare> findBySharedUser(User sharedUser);

    // Belirli bir listeyle ilgili tüm paylaşımları bulma
    List<ListShare> findByShoppingList(ShoppingList shoppingList);

    // Belirli bir kullanıcının belirli bir izin seviyesine sahip olduğu listeleri bulma
    List<ListShare> findBySharedUserAndPermissionLevel(User sharedUser, PermissionLevel permissionLevel);
}