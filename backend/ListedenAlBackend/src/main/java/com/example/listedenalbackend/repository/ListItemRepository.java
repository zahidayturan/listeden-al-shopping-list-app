package com.example.listedenalbackend.repository;

import com.example.listedenalbackend.model.ListItem;
import com.example.listedenalbackend.model.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ListItemRepository extends JpaRepository<ListItem, Long> {

    // Belirli bir alışveriş listesindeki tüm öğeleri bulma
    List<ListItem> findByShoppingList(ShoppingList shoppingList);

    // Belirli bir alışveriş listesinde satın alınmamış öğeleri bulma
    List<ListItem> findByShoppingListAndIsPurchasedFalse(ShoppingList shoppingList);

    // Belirli bir alışveriş listesinde ürün adına göre öğe bulma
    List<ListItem> findByShoppingListAndProductNameContainingIgnoreCase(ShoppingList shoppingList, String productName);
}