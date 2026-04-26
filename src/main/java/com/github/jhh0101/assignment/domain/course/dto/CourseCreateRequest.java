package com.github.jhh0101.assignment.domain.course.dto;

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
public class CourseCreateRequest {
    @Schema(description = "강의 제목", example = "JAVA SpringBoot")
    @NotBlank(message = "강의 제목을 입력해주세요.")
    private String title;

    @Schema(description = "강의 상세 내용", example = "Spring Boot 초급을 배웁니다.")
    @NotBlank(message = "강의 상세 내용을 입력해주세요.")
    private String description;

    @Schema(description = "강의 가격 (0원 이상)", example = "150000")
    @NotNull(message = "강의 가격을 입력해주세요.")
    @Min(value = 0, message = "최소 0원 이상이어야 합니다.")
    private Integer price;

    @Schema(description = "강의 최대 인원", example = "50")
    @NotNull(message = "강의 최대 인원을 입력해주세요.")
    @Min(value = 1, message = "최대 인원은 최소 1명 이상이어야 합니다.")
    private Integer maxCapacity;

    @Schema(description = "강의 시작 날짜", example = "2026-05-01T09:00:00")
    @NotNull(message = "강의 시작 날짜를 입력해주세요.")
    private LocalDateTime startTime;

    @Schema(description = "강의 종료 날짜", example = "2027-05-01T09:00:00")
    @NotNull(message = "강의 종료 날짜를 입력해주세요.")
    private LocalDateTime endTime;
}
