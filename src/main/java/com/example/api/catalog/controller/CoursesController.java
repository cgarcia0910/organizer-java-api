package com.example.api.catalog.controller;

import com.example.api.CoursesApi;
import com.example.api.catalog.service.CourseService;
import com.example.dto.Course;
import com.example.dto.CourseCreate;
import com.example.dto.CoursePage;
import com.example.dto.CoursePatch;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CoursesController implements CoursesApi {
    private final CourseService courseService;
    public CoursesController(CourseService courseService) {
        this.courseService = courseService;
    }
    @Override
    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR')")
    public ResponseEntity<Course> createCourse(CourseCreate courseCreate) {
        return ResponseEntity.ok(this.courseService.createCourse(courseCreate));
    }

    @Override
    @PreAuthorize("hasRole('CATALOG_ADMIN')")
    public ResponseEntity<Void> deactivateCourse(UUID courseId) {
        this.courseService.deactivateCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR','CATALOG_READER','TEACHER')")
    public ResponseEntity<Course> getCourse(UUID courseId) {
        return ResponseEntity.ok(this.courseService.getCourse(courseId));
    }

    @Override
    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR','CATALOG_READER','TEACHER')")
    public ResponseEntity<CoursePage> listCourses(Integer page, Integer size, @Nullable String search, Boolean includeInactive, String sort, String direction) {
        return  ResponseEntity.ok(this.courseService.listCourses(page, size, search, includeInactive, sort, direction));
    }

    @Override
    @PreAuthorize("hasRole('CATALOG_ADMIN')")
    public ResponseEntity<Course> reactivateCourse(UUID courseId) {
        return ResponseEntity.ok(this.courseService.reactivateCourse(courseId));
    }

    @Override
    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR')")
    public ResponseEntity<Course> updateCourse(UUID courseId, CoursePatch coursePatch) {
        return ResponseEntity.ok(this.courseService.updateCourse(courseId, coursePatch));
    }
}