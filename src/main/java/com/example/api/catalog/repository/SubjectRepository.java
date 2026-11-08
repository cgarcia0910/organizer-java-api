package com.example.api.catalog.repository;

import com.example.api.catalog.entity.Course;
import com.example.api.catalog.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID>, JpaSpecificationExecutor<Subject> {
    List<Subject> findAllByIdInAndTenantIdAndCourseId(Collection<UUID> ids, UUID tenantId, UUID courseId);

}
