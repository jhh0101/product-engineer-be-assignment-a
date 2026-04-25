package com.github.jhh0101.assignment.domain.enrollment.error;

import com.github.jhh0101.assignment.domain.enrollment.dto.WaitlistResponse;
import com.github.jhh0101.assignment.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.github.jhh0101.assignment.domain.enrollment")
public class EnrollmentExceptionHandler {

    @ExceptionHandler(WaitlistRegisteredException.class)
    public ResponseEntity<ApiResponse<WaitlistResponse>> handleWaitlistRegistered(WaitlistRegisteredException e) {

        log.info("대기열 등록 처리 완료 - 대기 순번: {}", e.getWaitNumber());

        WaitlistResponse waitlistResponse= new WaitlistResponse(e.getUserId(), e.getName(), e.getTitle(), e.getWaitNumber(), e.getTotalWaitingCount());

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("정원이 초과되어 대기열에 등록되었습니다.", waitlistResponse));
    }
}