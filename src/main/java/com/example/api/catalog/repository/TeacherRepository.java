package com.example.api.catalog.repository;
import com.example.api.catalog.entity.Teacher;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TeacherRepository extends JpaRepository<Teacher, UUID>, JpaSpecificationExecutor<Teacher> {
    Optional<Teacher> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Teacher> getByTenantIdAndEmailIgnoreCase(UUID tenantId, @NotNull @Size(max = 254) @Email String email);
}
