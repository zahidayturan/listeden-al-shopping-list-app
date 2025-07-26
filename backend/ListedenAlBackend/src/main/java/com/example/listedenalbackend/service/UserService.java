package com.example.listedenalbackend.service;

import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.model.Role;
import com.example.listedenalbackend.model.UserRole;
import com.example.listedenalbackend.repository.UserRepository;
import com.example.listedenalbackend.repository.RoleRepository;
import com.example.listedenalbackend.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Parola hashleme için
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder; // Security config'inizde tanımlanmalı

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(User user) {
        // Kullanıcı adı veya e-posta zaten var mı kontrol et
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        }

        // Parolayı hash'le
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        // Oluşturulma ve güncelleme zamanlarını ayarla (PrePersist'e rağmen manuel ayar tutarlı olabilir)
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Kullanıcıyı kaydet
        User savedUser = userRepository.save(user);

        // Varsayılan olarak ROLE_USER rolünü ata
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER"))); // Eğer yoksa oluştur
        userRoleRepository.save(new UserRole(savedUser, userRole));

        // Kullanıcının rollerini de yükle (eğer lazy fetch ise)
        savedUser.setUserRoles(userRoleRepository.findByUser(savedUser).stream().collect(Collectors.toSet()));

        return savedUser;
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id).map(existingUser -> {
            // Sadece güncellenebilecek alanları ayarla
            if (userDetails.getUsername() != null && !userDetails.getUsername().equals(existingUser.getUsername())) {
                if (userRepository.existsByUsername(userDetails.getUsername())) {
                    throw new IllegalArgumentException("Username already taken: " + userDetails.getUsername());
                }
                existingUser.setUsername(userDetails.getUsername());
            }
            if (userDetails.getEmail() != null && !userDetails.getEmail().equals(existingUser.getEmail())) {
                if (userRepository.existsByEmail(userDetails.getEmail())) {
                    throw new IllegalArgumentException("Email already registered: " + userDetails.getEmail());
                }
                existingUser.setEmail(userDetails.getEmail());
            }
            if (userDetails.getFirstName() != null) {
                existingUser.setFirstName(userDetails.getFirstName());
            }
            if (userDetails.getLastName() != null) {
                existingUser.setLastName(userDetails.getLastName());
            }
            // Şifre güncellenmesi ayrı bir metotla yapılmalı veya burada hashlenmeli
            // existingUser.setPasswordHash(userDetails.getPasswordHash()); // Eğer yeni şifre verilirse hashlenmeli
            existingUser.setUpdatedAt(LocalDateTime.now()); // PreUpdate de var ama manuel ayar güvenli

            return userRepository.save(existingUser);
        }).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Transactional
    public void deleteUser(Long id) {
        // İlişkili UserRole'ları da siler (CascadeType.ALL ve orphanRemoval=true sayesinde)
        userRepository.deleteById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}