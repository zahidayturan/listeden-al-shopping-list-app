package com.example.listedenalbackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "list_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    @JsonBackReference
    private ShoppingList shoppingList;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Double quantity; // Miktar için Double kullanmak ondalık değerlere izin verir

    private String unit; // Örn: "kg", "adet", "litre"

    @Column(nullable = false)
    private boolean isPurchased = false; // Varsayılan olarak satın alınmadı

    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id")
    private User addedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchased_by_user_id")
    private User purchasedBy;

    private Integer priority; // 1 (yüksek) - 5 (düşük) gibi

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}