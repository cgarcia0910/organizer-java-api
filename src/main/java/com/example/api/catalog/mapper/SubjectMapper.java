package com.example.api.catalog.mapper;

import com.example.api.catalog.entity.Subject;
import com.example.dto.SubjectStatus;
import org.springframework.stereotype.Component;

@Component
public class SubjectMapper {
    public com.example.dto.Subject toDto(Subject subject) {
        com.example.dto.Subject dto = new com.example.dto.Subject();
        dto.setId(subject.getId());
        dto.setCourseId(subject.getCourseId());
        dto.setCode(subject.getCode());
        dto.setName(subject.getName());
        dto.setHoursPerWeek(subject.getHoursPerWeek());
        dto.setMaxHoursPerDay(subject.getMaxHoursPerDay());
        dto.setDescription(subject.getDescription());
        dto.setStatus(SubjectStatus.fromValue(subject.getStatus()));
        dto.setCreatedAt(subject.getCreatedAt());
        dto.setUpdatedAt(subject.getUpdatedAt());
        return dto;
    }
}
