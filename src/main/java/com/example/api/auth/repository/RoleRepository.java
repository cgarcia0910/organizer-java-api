package com.example.api.auth.repository;

import com.example.api.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository
        extends JpaRepository<Role, Long> {
}