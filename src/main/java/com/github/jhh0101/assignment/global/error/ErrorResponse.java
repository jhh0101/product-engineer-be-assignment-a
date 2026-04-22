package com.github.jhh0101.assignment.global.error;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

import java.util.List;

public record ErrorResponse(
        int status,
        String code,
        String Error,

        @JsonInclude(JsonInclude.Include.NON_EMPTY) // 에러 발생 시에만 JSON에 포함
        List<ValidationError> validationErrors
) {
    // 일반 커스텀 예외용 생성 메서드
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }

    // Validation 예외용 생성 메서드
    public static ErrorResponse of(ErrorCode errorCode, List<ValidationError> validationErrors) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                validationErrors
        );
    }

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }

    // 필드 에러를 담을 내부 레코드
    public record ValidationError(String field, String message) {
        public static ValidationError from(FieldError fieldError) {
            return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
    }
}
