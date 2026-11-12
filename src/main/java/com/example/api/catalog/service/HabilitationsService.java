package com.example.api.catalog.service;

import com.example.api.auth.RoleContext;
import com.example.api.auth.Roles;
import com.example.api.auth.TenantContext;
import com.example.api.catalog.entity.Course;
import com.example.api.catalog.entity.Habilitations;
import com.example.api.catalog.entity.Subject;
import com.example.api.catalog.entity.Teacher;
import com.example.api.catalog.exception.ResourceNotFoundException;
import com.example.api.catalog.mapper.HabilitationMapper;
import com.example.api.catalog.repository.CoursesRepository;
import com.example.api.catalog.repository.HabilitationsRepository;
import com.example.api.catalog.repository.SubjectRepository;
import com.example.api.catalog.repository.TeacherRepository;
import com.example.dto.*;
import jakarta.validation.Valid;
import com.example.api.catalog.exception.ConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
@Service
public class HabilitationsService {
    private final TeacherRepository teacherRepository;
    private final HabilitationsRepository habilitationsRepository;
    private final SubjectRepository subjectRepository;
    private final CoursesRepository coursesRepository;
    public HabilitationsService(TeacherRepository teacherRepository, HabilitationsRepository habilitationsRepository,
                                SubjectRepository subjectRepository, CoursesRepository coursesRepository) {
        this.teacherRepository = teacherRepository;
        this.habilitationsRepository = habilitationsRepository;
        this.subjectRepository = subjectRepository;
        this.coursesRepository = coursesRepository;
    }

    @Transactional
    public List<Habilitation> createHabilitations(UUID teacherId, List<@Valid HabilitationCreate> habilitationCreate) {
        UUID tenantId = TenantContext.getTenantId();
        Teacher teacher = this.teacherRepository.findByIdAndTenantId(teacherId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        if(TeacherStatus.INACTIVE.toString().equals(teacher.getStatus())) {
            throw new ConflictException("Teacher must be an active teacher");
        }
        List<Habilitations> habilitations = habilitationCreate.stream().map(hc -> Habilitations.builder()
                .tenantId(tenantId)
                .teacherId(teacherId)
                .courseId(hc.getCourseId())
                .subjectId(hc.getSubjectId())
                .status("ACTIVE")
                .build()
        ).toList();
        Set<List<UUID>> seen = new HashSet<>();
        habilitations.forEach(habilitation -> {
            if (!seen.add(List.of(habilitation.getCourseId(), habilitation.getSubjectId()))) {
                throw new ConflictException("Duplicate course/subject combination in the request");
            }
            Course course = this.coursesRepository.findByIdAndTenantId(habilitation.getCourseId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            if (!"ACTIVE".equals(course.getStatus())) {
                throw new ConflictException("Courses must be active courses");
            }
            if (this.habilitationsRepository.existsByTenantIdAndTeacherIdAndCourseIdAndSubjectIdAndStatus(
                    tenantId, teacherId, habilitation.getCourseId(), habilitation.getSubjectId(), "ACTIVE")) {
                throw new ConflictException("The teacher already has an active habilitation for this course and subject");
            }
            Subject subject = this.subjectRepository
                    .findByIdAndTenantIdAndCourseId(habilitation.getSubjectId(), tenantId, habilitation.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found in the given course"));
            if(SubjectStatus.INACTIVE.toString().equals(subject.getStatus())) {
                throw new ConflictException("Subjects must be active subjects");
            }
        });
        return this.habilitationsRepository.saveAll(habilitations).stream().map(HabilitationMapper::toDto).toList();
    }

    public HabilitationPage listHabilitation(UUID teacherId, Integer page, Integer size, Boolean includeInactive) {
        UUID tenantId = TenantContext.getTenantId();
        boolean isAdmin = RoleContext.hasRole(Roles.CATALOG_ADMIN);

        Pageable pageable = PageRequest.of(
                page,
                size
        );

        Specification<Habilitations> spec = (root, query, cb) -> cb.and(
            cb.equal(root.get("tenantId"), tenantId),
            cb.equal(root.get("status"), "ACTIVE"),
            cb.equal(root.get("teacherId"), teacherId)
        );

        Page<Habilitations> habilitations = habilitationsRepository.findAll(spec, pageable);
        return new HabilitationPage(
                habilitations.getContent().stream().map(HabilitationMapper::toDto).toList(),
                habilitations.getNumber(),
                habilitations.getSize(),
                (int) habilitations.getTotalElements(),
                habilitations.getTotalPages()
        );
    }
}
