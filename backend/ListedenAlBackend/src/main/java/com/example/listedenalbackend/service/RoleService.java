package com.example.listedenalbackend.service;

import com.example.listedenalbackend.model.Role;
import com.example.listedenalbackend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    @Transactional
    public Role createRole(Role role) {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new IllegalArgumentException("Role with name " + role.getName() + " already exists.");
        }
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }
}