package com.example.api.auth.repository;
import com.example.api.auth.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository
        extends JpaRepository<AppUser, UUID> {
    @EntityGraph(attributePaths = "roles")
    Optional<AppUser> findByEmail(String email);
}