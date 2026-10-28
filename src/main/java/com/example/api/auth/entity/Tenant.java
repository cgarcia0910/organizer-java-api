package com.example.api.auth.entity;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    protected Tenant() {}

}