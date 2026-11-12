package com.example.api.catalog.mapper;

import com.example.api.catalog.entity.Course;
import com.example.dto.CourseStatus;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
    public com.example.dto.Course toDto(Course course) {
        com.example.dto.Course dto = new com.example.dto.Course();
        dto.setId(course.getId());
        dto.setCode(course.getCode());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setStatus(CourseStatus.fromValue(course.getStatus()));
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        return dto;
    }
}
