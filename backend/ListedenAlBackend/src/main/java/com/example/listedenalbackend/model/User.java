package com.example.listedenalbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
@Entity
@Table(name = "users")
@Data // Bu, @Getter, @Setter, @EqualsAndHashCode ve @ToString'i içerir
@NoArgsConstructor // Parametresiz constructor oluşturur (JPA için gereklidir)
@AllArgsConstructor // Tüm alanları parametre olarak alan bir constructor oluşturur
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
}