package com.github.jhh0101.assignment.domain.enrollment.controller;

import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentRegistrationResponse;
import com.github.jhh0101.assignment.domain.enrollment.service.EnrollmentService;
import com.github.jhh0101.assignment.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enrollment")
@Tag(name = "수강 신청 API", description = "수강 신청, 취소, 목록 조회, 결제 확정을 담당합니다.")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @Operation(summary = "강의 수강 신청", description = "사용자(userId)와 신청할 강의(courseId)를 받아서 수강 신청을 합니다..")
    @PostMapping("/{courseId}")
    public ResponseEntity<ApiResponse<EnrollmentRegistrationResponse>> courseRegistration(@RequestParam Long userId, @PathVariable Long courseId) {
        EnrollmentRegistrationResponse response = enrollmentService.courseRegistration(userId, courseId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("수강 신청 성공", response));
    }
}
