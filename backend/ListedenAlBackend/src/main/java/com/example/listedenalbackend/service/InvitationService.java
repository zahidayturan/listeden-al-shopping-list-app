package com.example.listedenalbackend.service;

import com.example.listedenalbackend.model.Invitation;
import com.example.listedenalbackend.model.Invitation.InvitationStatus;
import com.example.listedenalbackend.model.ListShare;
import com.example.listedenalbackend.model.ShoppingList;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.repository.InvitationRepository;
import com.example.listedenalbackend.repository.ListShareRepository;
import com.example.listedenalbackend.repository.ShoppingListRepository;
import com.example.listedenalbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Davetiye kodu için

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final UserRepository userRepository;
    private final ListShareRepository listShareRepository;

    @Autowired
    public InvitationService(InvitationRepository invitationRepository,
                             ShoppingListRepository shoppingListRepository,
                             UserRepository userRepository,
                             ListShareRepository listShareRepository) {
        this.invitationRepository = invitationRepository;
        this.shoppingListRepository = shoppingListRepository;
        this.userRepository = userRepository;
        this.listShareRepository = listShareRepository;
    }

    public List<Invitation> getAllInvitations() {
        return invitationRepository.findAll();
    }

    public Optional<Invitation> getInvitationById(Long id) {
        return invitationRepository.findById(id);
    }

    public Optional<Invitation> getInvitationByCode(String code) {
        return invitationRepository.findByInvitationCode(code);
    }

    public List<Invitation> getPendingInvitationsForUser(Long recipientUserId) {
        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient user not found with id: " + recipientUserId));
        return invitationRepository.findByRecipientUserAndStatus(recipient, InvitationStatus.PENDING);
    }

    // Kullanıcıya e-posta ile davetiye gönderme (e-posta gönderme mantığı burada değil)
    @Transactional
    public Invitation createInvitation(Long shoppingListId, String recipientEmail, Long senderId, ListShare.PermissionLevel permissionLevel) {
        ShoppingList shoppingList = shoppingListRepository.findById(shoppingListId)
                .orElseThrow(() -> new IllegalArgumentException("ShoppingList not found with id: " + shoppingListId));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender user not found with id: " + senderId));

        // Yetki kontrolü: Sadece liste sahibi veya ADMIN yetkisine sahip olan davetiye gönderebilir
        if (!hasAdminPermission(shoppingList, senderId)) {
            throw new SecurityException("User does not have permission to send invitations for this shopping list.");
        }

        // Eğer alıcı zaten kayıtlı bir kullanıcı ise
        Optional<User> recipientUserOptional = userRepository.findByEmail(recipientEmail);
        if (recipientUserOptional.isPresent()) {
            User recipientUser = recipientUserOptional.get();
            // Zaten listeye erişimi varsa davetiye gönderme
            if (shoppingList.getOwner().getId().equals(recipientUser.getId()) ||
                    listShareRepository.findByShoppingListAndSharedUser(shoppingList, recipientUser).isPresent()) {
                throw new IllegalArgumentException("User " + recipientEmail + " already has access to this list.");
            }
        }

        // Aynı e-postaya aynı liste için bekleyen davetiye var mı kontrol et
        if (invitationRepository.findByRecipientEmailAndStatus(recipientEmail, InvitationStatus.PENDING).stream()
                .anyMatch(inv -> inv.getShoppingList().getId().equals(shoppingListId))) {
            throw new IllegalArgumentException("Pending invitation already exists for this email and list.");
        }


        Invitation invitation = new Invitation();
        invitation.setShoppingList(shoppingList);
        invitation.setSender(sender);
        invitation.setRecipientEmail(recipientEmail);
        invitation.setRecipientUser(recipientUserOptional.orElse(null)); // Kayıtlı kullanıcı ise ata
        invitation.setInvitationCode(UUID.randomUUID().toString()); // Benzersiz kod
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setSentAt(LocalDateTime.now());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 gün sonra sona ersin

        return invitationRepository.save(invitation);
    }

    @Transactional
    public Invitation acceptInvitation(String invitationCode, Long acceptingUserId) {
        Invitation invitation = invitationRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired invitation code."));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is not in PENDING status.");
        }
        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new IllegalArgumentException("Invitation has expired.");
        }

        User acceptingUser = userRepository.findById(acceptingUserId)
                .orElseThrow(() -> new IllegalArgumentException("Accepting user not found with id: " + acceptingUserId));

        // Davetiyedeki e-posta ile kabul eden kullanıcının e-postası eşleşiyor mu kontrol et
        if (!invitation.getRecipientEmail().equalsIgnoreCase(acceptingUser.getEmail())) {
            throw new SecurityException("Invitation is not intended for this user's email address.");
        }

        // Zaten listeye erişimi varsa tekrar ekleme
        if (invitation.getShoppingList().getOwner().getId().equals(acceptingUser.getId()) ||
                listShareRepository.findByShoppingListAndSharedUser(invitation.getShoppingList(), acceptingUser).isPresent()) {
            invitation.setStatus(InvitationStatus.REJECTED); // Ya da 'ALREADY_ACCEPTED' gibi bir durum ekleyebiliriz
            invitationRepository.save(invitation);
            throw new IllegalArgumentException("User " + acceptingUser.getEmail() + " already has access to this list.");
        }


        // ListShare kaydı oluştur
        ListShare listShare = new ListShare();
        listShare.setShoppingList(invitation.getShoppingList());
        listShare.setSharedUser(acceptingUser);
        listShare.setPermissionLevel(ListShare.PermissionLevel.EDITOR); // Davetiye ile varsayılan olarak EDITOR yetkisi veriyoruz.
        // İstenirse davetiye oluştururken bu seviye belirlenebilir.
        listShareRepository.save(listShare);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitation.setRecipientUser(acceptingUser); // Davetiye kabul edildiğinde recipientUser'ı ayarla

        return invitationRepository.save(invitation);
    }

    @Transactional
    public Invitation rejectInvitation(String invitationCode, Long rejectingUserId) {
        Invitation invitation = invitationRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation code."));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is not in PENDING status.");
        }

        User rejectingUser = userRepository.findById(rejectingUserId)
                .orElseThrow(() -> new IllegalArgumentException("Rejecting user not found with id: " + rejectingUserId));

        if (!invitation.getRecipientEmail().equalsIgnoreCase(rejectingUser.getEmail())) {
            throw new SecurityException("Invitation is not intended for this user's email address.");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        return invitationRepository.save(invitation);
    }

    @Transactional
    public void deleteInvitation(Long id, Long currentUserId) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found with id: " + id));

        // Yetki kontrolü: Davetiyeyi gönderen veya liste sahibi silebilir
        if (!invitation.getSender().getId().equals(currentUserId) &&
                !hasAdminPermission(invitation.getShoppingList(), currentUserId)) {
            throw new SecurityException("User does not have permission to delete this invitation.");
        }

        invitationRepository.deleteById(id);
    }

    // Yardımcı metot: Kullanıcının belirli bir liste üzerinde ADMIN yetkisi var mı?
    private boolean hasAdminPermission(ShoppingList shoppingList, Long userId) {
        if (shoppingList.getOwner().getId().equals(userId)) {
            return true;
        }
        return listShareRepository.findByShoppingListAndSharedUser(shoppingList, userRepository.findById(userId).orElse(null))
                .map(share -> share.getPermissionLevel() == ListShare.PermissionLevel.ADMIN)
                .orElse(false);
    }
}