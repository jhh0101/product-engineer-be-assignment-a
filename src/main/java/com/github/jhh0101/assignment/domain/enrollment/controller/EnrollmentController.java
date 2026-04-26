package com.github.jhh0101.assignment.domain.enrollment.controller;

import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentCancelledResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentConfirmedResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentListResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentRegistrationResponse;
import com.github.jhh0101.assignment.domain.enrollment.service.EnrollmentService;
import com.github.jhh0101.assignment.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enrollment")
@Tag(name = "수강 신청 API", description = "수강 신청, 취소, 목록 조회, 결제 확정을 담당합니다.")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @Operation(summary = "강의 수강 신청", description = "사용자(userId)와 신청할 강의(courseId)를 받아서 수강 신청을 합니다.")
    @PostMapping("/{courseId}")
    public ResponseEntity<ApiResponse<EnrollmentRegistrationResponse>> courseRegistration(@RequestParam Long userId, @PathVariable Long courseId) {
        EnrollmentRegistrationResponse response = enrollmentService.courseRegistration(userId, courseId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("수강 신청 성공", response));
    }

    @Operation(summary = "강의 결제 확정", description = "사용자(userId)와 결제 확정할 강의(enrollmentId)를 받아서 결제를 확정 합니다.")
    @PatchMapping("/confirmed/{enrollmentId}")
    public ResponseEntity<ApiResponse<EnrollmentConfirmedResponse>> enrollmentConfirmed(@RequestParam Long userId, @PathVariable Long enrollmentId) {
        EnrollmentConfirmedResponse response = enrollmentService.enrollmentConfirmed(userId, enrollmentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("강의 결제 확정 성공", response));
    }

    @Operation(summary = "강의 수강 취소", description = "사용자(userId)와 수강 취소할 강의(enrollmentId)를 받아서 수강을 취소 합니다.")
    @PatchMapping("/cancelled/{enrollmentId}")
    public ResponseEntity<ApiResponse<EnrollmentCancelledResponse>> enrollmentCancelled(@RequestParam Long userId, @PathVariable Long enrollmentId) {
        EnrollmentCancelledResponse response = enrollmentService.enrollmentCancelled(userId, enrollmentId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("강의 수강 취소 성공", response));
    }

    @Operation(summary = "내 수강 신청 목록 조회", description = "사용자(userId)를 받아서 사용자의 수강 신청 목록을 조회합니다.")
    @GetMapping("/my/list")
    public ResponseEntity<ApiResponse<Page<EnrollmentListResponse>>> myEnrollmentList(@RequestParam Long userId, @PageableDefault(size = 10) Pageable pageable) {
        Page<EnrollmentListResponse> responses = enrollmentService.myEnrollmentList(userId, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("내 신청 목록 조회 성공", responses));
    }

    @Operation(summary = "강의별 수강생 목록 조회", description = "사용자(userId)를 받아서 모든 사용자의 수강 신청 목록을 조회합니다.(CREATOR 권한 필수)")
    @GetMapping("/user/list/{courseId}")
    public ResponseEntity<ApiResponse<Page<EnrollmentListResponse>>> userEnrollmentList(@RequestParam Long userId, @PathVariable Long courseId, @PageableDefault(size = 10) Pageable pageable) {
        Page<EnrollmentListResponse> responses = enrollmentService.userEnrollmentList(userId, courseId, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("강의별 수강생 목록 조회 성공", responses));
    }
}
