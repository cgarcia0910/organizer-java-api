package com.example.api.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Setter
    private String name;
    @Setter
    private String email;

    public User() {
    }

}