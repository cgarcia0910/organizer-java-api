package com.example.api.catalog.mapper;

import com.example.api.catalog.entity.Teacher;
import com.example.dto.HabilitationStatus;
import com.example.dto.TeacherStatus;


public class TeacherMapper {
    public static com.example.dto.Teacher toDto(Teacher teacher) {
        return new com.example.dto.Teacher(
                teacher.getId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getEmail(),
                TeacherStatus.fromValue(teacher.getStatus()),
                teacher.getCreatedAt(),
                teacher.getUpdatedAt()
        );
    }
}
