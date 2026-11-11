package com.example.api.catalog.repository;

import com.example.api.catalog.entity.Habilitations;
import com.example.api.catalog.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface HabilitationsRepository extends JpaRepository<Habilitations, UUID>, JpaSpecificationExecutor<Habilitations> {
    List<Habilitations> findAllByIdInAndTenantIdAndTeacherId(Collection<UUID> ids, UUID tenantId, UUID teacherId);

    boolean existsByTenantIdAndTeacherIdAndCourseIdAndSubjectIdAndStatus(
            UUID tenantId, UUID teacherId, UUID courseId, UUID subjectId, String status);

}
