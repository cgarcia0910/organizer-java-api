package com.example.api.catalog.controller;

import com.example.api.TeachersApi;
import com.example.api.catalog.service.TeacherService;
import com.example.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TeachersController implements TeachersApi {
    TeacherService teacherService;
    public TeachersController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR')")
    @Override
    public ResponseEntity<Teacher> createTeacher(TeacherCreate teacherCreate) {
        return ResponseEntity.ok(teacherService.createTeacher(teacherCreate));
    }

    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR')")
    @Override
    public ResponseEntity<TeacherPage> listTeachers(@Min(value = 0) @Valid Integer page, @Min(value = 1) @Max(value = 100) @Valid Integer size, @Size(min = 1, max = 100) @Valid String search, @Valid TeacherStatus status, @Valid String sort, @Valid String direction) {
        return ResponseEntity.ok(this.teacherService.listTeachers(page, size, search, false, sort, direction));
    }

    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR')")
    @Override
    public ResponseEntity<Teacher> updateTeacher(UUID teacherId, TeacherPatch teacherPatch) {
        return ResponseEntity.ok(this.teacherService.updateTeacher(teacherId, teacherPatch));
    }
}
