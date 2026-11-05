package com.example.api.auth.repository;

import com.example.api.auth.entity.Tenant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository
        extends JpaRepository<Tenant, UUID> {
}
