package com.example.api.catalog.mapper;

import com.example.api.catalog.entity.Habilitations;
import com.example.api.catalog.entity.Subject;
import com.example.dto.HabilitationStatus;
import com.example.dto.SubjectStatus;
import org.springframework.stereotype.Component;

@Component
public class HabilitationMapper {
    public static com.example.dto.Habilitation toDto(Habilitations habilitations) {
        com.example.dto.Habilitation dto = new com.example.dto.Habilitation();
        dto.setId(habilitations.getId());
        dto.setCourseId(habilitations.getCourseId());
        dto.setSubjectId(habilitations.getSubjectId());
        dto.setStatus(HabilitationStatus.fromValue(habilitations.getStatus()));
        dto.setCreatedAt(habilitations.getCreatedAt());
        dto.setUpdatedAt(habilitations.getUpdatedAt());
        return dto;
    }
}
