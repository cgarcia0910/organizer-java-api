package com.example.api.catalog.controller;

import com.example.api.HabilitationsApi;
import com.example.api.catalog.service.HabilitationsService;
import com.example.dto.Habilitation;
import com.example.dto.HabilitationCreate;
import com.example.dto.HabilitationPage;
import com.example.dto.HabilitationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class HabilitationsController implements HabilitationsApi {
    HabilitationsService habilitationService;
    public  HabilitationsController(HabilitationsService habilitationsService) {
        this.habilitationService = habilitationsService;
    }

    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR')")
    @Override
    public ResponseEntity<List<Habilitation>> createHabilitations(UUID teacherId, @Size(min = 1) @Valid List<@Valid HabilitationCreate> habilitationCreate) {
        return ResponseEntity.ok(this.habilitationService.createHabilitations(teacherId, habilitationCreate));
    }

    @PreAuthorize("hasAnyRole('CATALOG_ADMIN','CATALOG_EDITOR')")
    @Override
    public ResponseEntity<HabilitationPage> listTeacherHabilitations(UUID teacherId, @Min(value = 0) @Valid Integer page, @Min(value = 1) @Max(value = 100) @Valid Integer size, @Valid HabilitationStatus status, @Valid Boolean effective) {
        return ResponseEntity.ok(this.habilitationService.listHabilitation(teacherId, page, size, false ));
    }
}
