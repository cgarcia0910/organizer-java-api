package com.example.api.catalog.service;

import com.example.api.auth.RoleContext;
import com.example.api.auth.Roles;
import com.example.api.auth.TenantContext;
import com.example.api.catalog.entity.Course;
import com.example.api.catalog.exception.ConflictException;
import com.example.api.catalog.exception.ResourceNotFoundException;
import com.example.api.catalog.mapper.CourseMapper;
import com.example.api.catalog.repository.CoursesRepository;

import com.example.dto.CourseCreate;
import com.example.dto.CoursePage;
import com.example.dto.CoursePatch;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CourseService {
    private final CoursesRepository coursesRepository;
    private final CourseMapper courseMapper;
    public CourseService(CoursesRepository coursesRepository, CourseMapper courseMapper) {
        this.coursesRepository = coursesRepository;
        this.courseMapper = courseMapper;
    }
    public CoursePage listCourses(Integer page, Integer size, @Nullable String search, Boolean includeInactive, String sort, String direction) {
        UUID tenantId = TenantContext.getTenantId();
        boolean isAdmin = RoleContext.hasRole(Roles.CATALOG_ADMIN);
        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sort)
        );
        Specification<Course> spec = (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
        if (!(Boolean.TRUE.equals(includeInactive) && isAdmin)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), "ACTIVE"));
        }
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("name")), pattern)));
        }
        Page<Course> courses = coursesRepository.findAll(spec, pageable);

        return new CoursePage(
                courses.getContent()
                        .stream()
                        .map(courseMapper::toDto)
                        .toList(),
                courses.getNumber(),
                courses.getSize(),
                (int) courses.getTotalElements(),
                courses.getTotalPages()
        );
    }

    @Transactional
    public com.example.dto.Course createCourse(CourseCreate courseCreate) {
        Course course = new Course();
        UUID tenantId = TenantContext.getTenantId();
        course.setTenantId(tenantId);
        course.setCode(courseCreate.getCode());
        course.setDescription(courseCreate.getDescription());
        course.setName(courseCreate.getName());
        course.setStatus("ACTIVE");
        
        return  courseMapper.toDto(coursesRepository.save(course));
    }
    public com.example.dto.Course getCourse(UUID courseId) {
        UUID tenantId = TenantContext.getTenantId();
        return coursesRepository.findByIdAndTenantId(courseId, tenantId)
                .map(courseMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
    }

    @Transactional
    public com.example.dto.Course updateCourse(UUID courseId, CoursePatch patch) {
        UUID tenantId = TenantContext.getTenantId();
        Course course = coursesRepository.findByIdAndTenantId(courseId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        if (patch.getCode() != null && !patch.getCode().equals(course.getCode())) {
            if (coursesRepository.existsByTenantIdAndCode(tenantId, patch.getCode())) {
                throw new ConflictException("Ya existe un curso con ese código");
            }
            course.setCode(patch.getCode());
        }
        if (patch.getName() != null) {
            course.setName(patch.getName());
        }
        if (patch.getDescription() != null) {
            course.setDescription(patch.getDescription());
        }
        return courseMapper.toDto(course);
    }

    @Transactional
    public void deactivateCourse(UUID courseId) {
        findCourseInTenant(courseId).setStatus("INACTIVE");
    }

    @Transactional
    public com.example.dto.Course reactivateCourse(UUID courseId) {
        Course course = findCourseInTenant(courseId);
        course.setStatus("ACTIVE");
        return courseMapper.toDto(course);
    }

    private Course findCourseInTenant(UUID courseId) {
        return coursesRepository.findByIdAndTenantId(courseId, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
    }
}
