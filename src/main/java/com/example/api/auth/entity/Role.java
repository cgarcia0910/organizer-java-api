package com.example.api.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    @Getter
    @Column(nullable = false, unique = true)
    private String name;

    // Getters y setters
}