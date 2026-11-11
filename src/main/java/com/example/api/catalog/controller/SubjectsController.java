package com.example.api.catalog.controller;

import com.example.api.SubjectsApi;
import com.example.api.catalog.service.SubjectService;
import com.example.dto.*;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class SubjectsController implements SubjectsApi {
    private final SubjectService subjectService;

    public SubjectsController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Override
    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR')")
    public ResponseEntity<List<Subject>> createSubject(UUID courseId, List<SubjectCreate> subjectCreate) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.createSubject(courseId, subjectCreate));
    }

    @Override
    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR','CATALOG_READER','TEACHER')")
    public ResponseEntity<SubjectPage> listSubjects(UUID courseId, Integer page, Integer size, @Nullable String search, Boolean includeInactive, String sort, String direction) {
        return ResponseEntity.ok(subjectService.listSubjects(courseId, page, size, search, includeInactive, sort, direction));
    }

    @Override
    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR')")
    public ResponseEntity<List<Subject>> updateSubjects(UUID courseId, UpdateSubjectsRequest updateSubjectsRequest) {
        return ResponseEntity.ok(subjectService.updateSubjects(courseId, updateSubjectsRequest));
    }

}
