package com.example.listedenalbackend.repository;

import com.example.listedenalbackend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Rol adına göre rol bulma (örn: ROLE_USER)
    Optional<Role> findByName(String name);
}