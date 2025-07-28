package com.example.listedenalbackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "list_shares", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"shopping_list_id", "shared_user_id"}) // Bir kullanıcı bir liste için sadece bir paylaşıma sahip olabilir
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ListShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    @EqualsAndHashCode.Include // Eşsizlik kısıtlaması için dahil edildi
    @JsonBackReference
    private ShoppingList shoppingList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_user_id", nullable = false)
    @EqualsAndHashCode.Include // Eşsizlik kısıtlaması için dahil edildi
    private User sharedUser;

    @Enumerated(EnumType.STRING) // String olarak kaydet
    @Column(nullable = false)
    private PermissionLevel permissionLevel; // VIEW, EDIT, ADMIN gibi yetki seviyeleri

    @Column(nullable = false, updatable = false)
    private LocalDateTime sharedAt;

    @PrePersist
    protected void onCreate() {
        this.sharedAt = LocalDateTime.now();
    }

    public enum PermissionLevel {
        VIEWER,  // Sadece listeyi görüntüleyebilir
        EDITOR,  // Listeye ürün ekleyebilir, düzenleyebilir, satın alındı işaretleyebilir
        ADMIN    // Listeyi yönetebilir (adını değiştirme, silme, paylaşım yetkileri vb.)
    }
}