package com.example.api.catalog.service;

import com.example.api.auth.RoleContext;
import com.example.api.auth.Roles;
import com.example.api.auth.TenantContext;
import com.example.api.catalog.entity.Course;
import com.example.api.catalog.entity.Subject;
import com.example.api.catalog.exception.BadRequestException;
import com.example.api.catalog.exception.ResourceNotFoundException;
import com.example.api.catalog.mapper.SubjectMapper;
import com.example.api.catalog.repository.CoursesRepository;
import com.example.api.catalog.repository.SubjectRepository;
import com.example.dto.CoursePage;
import com.example.dto.SubjectCreate;
import com.example.dto.SubjectPage;
import com.example.dto.SubjectPatch;
import com.example.dto.UpdateSubjectsRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final CoursesRepository coursesRepository;
    private final SubjectMapper subjectMapper;

    public SubjectService(SubjectRepository subjectRepository, CoursesRepository coursesRepository, SubjectMapper subjectMapper) {
        this.subjectRepository = subjectRepository;
        this.coursesRepository = coursesRepository;
        this.subjectMapper = subjectMapper;
    }

    @Transactional
    public List<com.example.dto.Subject> createSubject(UUID courseId, List<SubjectCreate> subjectCreateList) {
        UUID tenantId = TenantContext.getTenantId();
        coursesRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
        List<Subject> subjects = subjectCreateList.stream().map(subjectCreate -> Subject.builder()
                .tenantId(tenantId)
                .courseId(courseId)
                .code(subjectCreate.getCode())
                .name(subjectCreate.getName())
                .description(subjectCreate.getDescription())
                .hoursPerWeek(subjectCreate.getHoursPerWeek() != null ? subjectCreate.getHoursPerWeek() : 0)
                .maxHoursPerDay(subjectCreate.getMaxHoursPerDay() != null ? subjectCreate.getMaxHoursPerDay() : 0)
                .build()).toList();

        return subjectRepository.saveAll(subjects).stream().map(subjectMapper::toDto).toList();
    }

    @Transactional
    public List<com.example.dto.Subject> updateSubjects(UUID courseId, UpdateSubjectsRequest updateSubjectsRequest) {
        UUID tenantId = TenantContext.getTenantId();
        coursesRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        List<SubjectCreate> toCreate = orEmpty(updateSubjectsRequest.getNew());
        List<SubjectPatch> patches = orEmpty(updateSubjectsRequest.getUpdate());
        Set<UUID> deleteIds = new HashSet<>(orEmpty(updateSubjectsRequest.getDelete()));

        if (patches.stream().anyMatch(patch -> patch.getId() == null)) {
            throw new BadRequestException("Cada asignatura a actualizar requiere id");
        }
        if (patches.stream().anyMatch(patch -> deleteIds.contains(patch.getId()))) {
            throw new BadRequestException("Una asignatura no puede actualizarse y eliminarse a la vez");
        }

        Map<UUID, Subject> toUpdate = findInCourse(patches.stream().map(SubjectPatch::getId).collect(Collectors.toSet()), tenantId, courseId);
        patches.forEach(patch -> applyPatch(toUpdate.get(patch.getId()), patch));

        findInCourse(deleteIds, tenantId, courseId).values()
                .forEach(subject -> subject.setStatus("INACTIVE"));

        List<com.example.dto.Subject> result = new ArrayList<>();
        if (!toCreate.isEmpty()) {
            result.addAll(createSubject(courseId, toCreate));
        }
        toUpdate.values().forEach(subject -> result.add(subjectMapper.toDto(subject)));
        return result;
    }

    private Map<UUID, Subject> findInCourse(Set<UUID> ids, UUID tenantId, UUID courseId) {
        Map<UUID, Subject> found = subjectRepository.findAllByIdInAndTenantIdAndCourseId(ids, tenantId, courseId).stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));
        if (found.size() != ids.size()) {
            throw new ResourceNotFoundException("Asignatura no encontrada");
        }
        return found;
    }

    private void applyPatch(Subject subject, SubjectPatch patch) {
        if (patch.getCode() != null) {
            subject.setCode(patch.getCode());
        }
        if (patch.getName() != null) {
            subject.setName(patch.getName());
        }
        if (patch.getHoursPerWeek() != null) {
            subject.setHoursPerWeek(patch.getHoursPerWeek());
        }
        if (patch.getMaxHoursPerDay() != null) {
            subject.setMaxHoursPerDay(patch.getMaxHoursPerDay());
        }
        if (patch.getDescription() != null) {
            subject.setDescription(patch.getDescription());
        }
    }

    private static <T> List<T> orEmpty(@Nullable List<T> list) {
        return list != null ? list : List.of();
    }

    public SubjectPage listSubjects(UUID courseId, Integer page, Integer size, @Nullable String search, Boolean includeInactive, String sort, String direction) {
        UUID tenantId = TenantContext.getTenantId();
        boolean isAdmin = RoleContext.hasRole(Roles.CATALOG_ADMIN);
        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sort)
        );

        Specification<Subject> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("tenantId"), tenantId),
                cb.equal(root.get("courseId"), courseId));
        if (!(Boolean.TRUE.equals(includeInactive) && isAdmin)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), "ACTIVE"));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("name")), pattern)));
        }
        Page<Subject> subjects = subjectRepository.findAll(spec, pageable);
        return new SubjectPage(
                subjects.getContent().stream()
                        .map(subjectMapper::toDto)
                        .toList(),
                subjects.getNumber(),
                subjects.getSize(),
                (int) subjects.getTotalElements(),
                subjects.getTotalPages()
        );
    }
}
