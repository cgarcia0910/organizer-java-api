package com.example.api.catalog.service;

import com.example.api.auth.RoleContext;
import com.example.api.auth.Roles;
import com.example.api.auth.TenantContext;
import com.example.api.catalog.entity.Course;
import com.example.api.catalog.entity.Teacher;
import com.example.api.catalog.exception.ResourceNotFoundException;
import com.example.api.catalog.mapper.TeacherMapper;
import com.example.api.catalog.repository.TeacherRepository;
import com.example.dto.TeacherCreate;
import com.example.dto.TeacherPage;
import com.example.dto.TeacherPatch;
import org.jspecify.annotations.Nullable;
import com.example.api.catalog.exception.ConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;
    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }
    public TeacherPage listTeachers(Integer page, Integer size, @Nullable String search, Boolean includeInactive, String sort, String direction) {
        UUID tenantId = TenantContext.getTenantId();
        boolean isAdmin = RoleContext.hasRole(Roles.CATALOG_ADMIN);
        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sort)
        );
        Specification<Teacher> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
        if (!(Boolean.TRUE.equals(includeInactive) && isAdmin)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), "ACTIVE"));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)));
        }
        Page<Teacher> teachers = teacherRepository.findAll(spec, pageable);
        return new TeacherPage(
                teachers.getContent().stream().map(TeacherMapper::toDto).toList(),
                teachers.getNumber(),
                teachers.getSize(),
                (int) teachers.getTotalElements(),
            teachers.getTotalPages()
        );
    }

    @Transactional
    public com.example.dto.Teacher createTeacher(TeacherCreate teacherCreate) {
        UUID tenantId = TenantContext.getTenantId();
        Optional<Teacher> existingTeacher = teacherRepository.getByTenantIdAndEmailIgnoreCase(tenantId, teacherCreate.getEmail());
        if(existingTeacher.isPresent()) {
            throw new ConflictException("A teacher with this email already exists");
        }
        Teacher teacher = Teacher.builder()
                .tenantId(tenantId)
                .firstName(teacherCreate.getFirstName())
                .lastName(teacherCreate.getLastName())
                .email(teacherCreate.getEmail())
                .status("ACTIVE").build();
        return TeacherMapper.toDto(teacherRepository.save(teacher));
    }

    @Transactional
    public com.example.dto.Teacher updateTeacher(UUID teacherId, TeacherPatch patch) {
        UUID tenantId = TenantContext.getTenantId();
        Teacher teacher = teacherRepository.findByIdAndTenantId(teacherId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher no encontrado"));
        if(patch.getFirstName() != null) {
            teacher.setFirstName(patch.getFirstName());
        }
        if(patch.getLastName() != null) {
            teacher.setLastName(patch.getLastName());
        }
        if(patch.getEmail() != null) {
            Optional<Teacher> existingTeacher = teacherRepository.getByTenantIdAndEmailIgnoreCase(tenantId, patch.getEmail());
            if(existingTeacher.isPresent() && !existingTeacher.get().getId().equals(teacherId)) {
                throw new ConflictException("A teacher with this email already exists");
            }
            teacher.setEmail(patch.getEmail());
        }
        return TeacherMapper.toDto(teacher);
    }


}
