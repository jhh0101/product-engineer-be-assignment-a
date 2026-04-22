package com.github.jhh0101.assignment.domain.course.dto;

import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateRequest {
    @Schema(description = "강의 제목", example = "JAVA SpringBoot")
    private String title;

    @Schema(description = "강의 상세 내용", example = "Spring Boot 초급을 배웁니다.")
    private String description;

    @Schema(description = "강의 가격 (0원 이상)", example = "150000")
    private Integer price;

    @Schema(description = "강의 최대 인원", example = "50")
    private Integer maxCapacity;

    @Schema(description = "강의 시작 날짜", example = "2026-05-01T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "강의 종료 날짜", example = "2027-05-01T09:00:00")
    private LocalDateTime endTime;

    @Schema(description = "강의 상태", example = "OPEN")
    private CourseStatus status;
}
