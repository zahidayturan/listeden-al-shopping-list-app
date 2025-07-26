package com.example.listedenalbackend.repository;

import com.example.listedenalbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Kullanıcı adıyla kullanıcı bulma
    Optional<User> findByUsername(String username);

    // E-posta adresiyle kullanıcı bulma
    Optional<User> findByEmail(String email);

    // Kullanıcı adının var olup olmadığını kontrol etme
    boolean existsByUsername(String username);

    // E-posta adresinin var olup olmadığını kontrol etme
    boolean existsByEmail(String email);
}