package com.github.jhh0101.assignment.domain.course.controller;

import com.github.jhh0101.assignment.domain.course.dto.CourseRequest;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
import com.github.jhh0101.assignment.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
@Tag(name = "강의 API", description = "강의 생성, 조회, 수정, 삭제를 담당합니다.")
public class CourseController {
    private final CourseService courseService;

    @Operation(summary = "새로운 강의 생성", description = "강의 정보를 입력받아 생성합니다.")
    @PostMapping("")
    public ResponseEntity<ApiResponse<CourseResponse>> courseCreate(@Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.courseCreate(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("강의 등록 성공", response));
    }
}
