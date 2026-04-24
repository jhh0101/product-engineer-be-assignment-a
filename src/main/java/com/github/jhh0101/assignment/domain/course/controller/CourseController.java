package com.github.jhh0101.assignment.domain.course.controller;

import com.github.jhh0101.assignment.domain.course.dto.CourseCreateRequest;
import com.github.jhh0101.assignment.domain.course.dto.CourseDetailResponse;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.dto.CourseUpdateRequest;
import com.github.jhh0101.assignment.domain.course.repository.CourseListCondition;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
import com.github.jhh0101.assignment.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
@Tag(name = "강의 API", description = "강의 생성, 조회, 수정, 삭제를 담당합니다.")
public class CourseController {
    private final CourseService courseService;

    @Operation(summary = "새로운 강의 생성", description = "강의 정보를 입력받아 생성합니다.")
    @PostMapping("")
    public ResponseEntity<ApiResponse<CourseResponse>> courseCreate(@RequestParam Long userId, @Valid @RequestBody CourseCreateRequest request) {
        CourseResponse response = courseService.courseCreate(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("강의 등록 성공", response));
    }

    @Operation(summary = "기존 강의 수정", description = "강의 정보를 입력받아 수정합니다.")
    @PatchMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> courseUpdate(@RequestParam Long userId, @PathVariable Long courseId, @RequestBody CourseUpdateRequest request) {
        CourseResponse response = courseService.courseUpdate(userId, courseId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("강의 수정 성공", response));
    }

    @Operation(summary = "강의 리스트 조회(상태 필터링)", description = "강의 리스트를 조회합니다.(상태별 검색 가능)")
    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> courseList(@ModelAttribute CourseListCondition condition, @PageableDefault(size = 10) Pageable pageable) {
        Page<CourseResponse> responses = courseService.courseList(condition, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("강의 리스트 조회 성공", responses));
    }

    @Operation(summary = "강의 상세 조회", description = "강의 상세 내용을 조회합니다.")
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> courseDetail(@PathVariable Long courseId) {
        CourseDetailResponse response = courseService.courseDetail(courseId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("강의 상세 조회 성공", response));
    }
}
