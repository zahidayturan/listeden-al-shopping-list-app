package com.example.listedenalbackend.repository;

import com.example.listedenalbackend.model.Invitation;
import com.example.listedenalbackend.model.ShoppingList;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.model.Invitation.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    // Davet koduna göre davetiye bulma
    Optional<Invitation> findByInvitationCode(String invitationCode);

    // Belirli bir alıcı e-postası için bekleyen davetiyeleri bulma
    List<Invitation> findByRecipientEmailAndStatus(String recipientEmail, InvitationStatus status);

    // Belirli bir kullanıcıya (kayıtlı) gönderilen bekleyen davetiyeleri bulma
    List<Invitation> findByRecipientUserAndStatus(User recipientUser, InvitationStatus status);

    // Belirli bir alışveriş listesi için tüm davetiyeleri bulma
    List<Invitation> findByShoppingList(ShoppingList shoppingList);

    // Bir gönderici tarafından gönderilen tüm davetiyeleri bulma
    List<Invitation> findBySender(User sender);
}