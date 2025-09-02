package com.example.listedenalbackend.service;

import com.example.listedenalbackend.dto.RegisterRequest;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.model.Role;
import com.example.listedenalbackend.model.UserRole;
import com.example.listedenalbackend.repository.UserRepository;
import com.example.listedenalbackend.repository.RoleRepository;
import com.example.listedenalbackend.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public User createUser(RegisterRequest newUser) {

        if (newUser.getPassword() == null || newUser.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty or null");
        }

        if (!isValidEmail(newUser.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new IllegalArgumentException("Kayıt işlemi sırasında bir hata oluştu");
        }
        
        User user = new User();
        user.setUsername(newUser.getUsername());
        user.setEmail(newUser.getEmail());

        String hashedPassword = passwordEncoder.encode(newUser.getPassword());
        user.setPasswordHash(hashedPassword);

        User savedUser = userRepository.save(user);

        // Varsayılan rol ataması
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        UserRole newUserRole = new UserRole(savedUser, userRole);
        userRoleRepository.save(newUserRole);
        return savedUser;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id).map(existingUser -> {
            // Sadece güncellenebilecek alanları ayarla
            if (userDetails.getUsername() != null && !userDetails.getUsername().equals(existingUser.getUsername())) {
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
            existingUser.setUpdatedAt(LocalDateTime.now());

            return userRepository.save(existingUser);
        }).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}