package com.example.listedenalbackend.repository;

import com.example.listedenalbackend.model.UserRole;
import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    // Bir kullanıcı ve rol kombinasyonuna göre UserRole bulma
    Optional<UserRole> findByUserAndRole(User user, Role role);

    // Bir kullanıcıya ait tüm UserRole'ları bulma
    List<UserRole> findByUser(User user);
}