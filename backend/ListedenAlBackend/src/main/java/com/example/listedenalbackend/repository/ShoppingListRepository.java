package com.example.listedenalbackend.repository;

import com.example.listedenalbackend.model.ShoppingList;
import com.example.listedenalbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    // Belirli bir kullanıcıya ait tüm alışveriş listelerini bulma (owner)
    List<ShoppingList> findByOwner(User owner);

    // Belirli bir liste adıyla listeleri bulma (örneğin, sahibine göre ad ile arama)
    Optional<ShoppingList> findByNameAndOwner(String name, User owner);

    // Arşivlenmemiş listeleri bulma
    List<ShoppingList> findByIsArchivedFalse();

    // Bir kullanıcı tarafından oluşturulan veya paylaşılan tüm listeleri bulma
    // Not: Bu karmaşık bir sorgu olabilir, @Query ile veya Service katmanında ListShareRepository kullanarak yapılabilir.
    // Şimdilik sadece owner için olanı ekledik.
}