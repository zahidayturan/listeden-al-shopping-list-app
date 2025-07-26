package com.example.listedenalbackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    private String recipientEmail; // Eğer kullanıcı henüz kayıtlı değilse

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id") // Eğer kullanıcı kayıtlı ise
    private User recipientUser;

    @Column(unique = true, nullable = false)
    private String invitationCode; // Kullanıcının daveti kabul etmek için kullanacağı benzersiz kod

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING; // Varsayılan durum

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    private LocalDateTime expiresAt; // Davetin son kullanma tarihi

    private LocalDateTime acceptedAt; // Davetin kabul edildiği tarih

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        EXPIRED
    }
}