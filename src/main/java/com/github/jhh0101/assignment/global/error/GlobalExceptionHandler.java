package com.github.jhh0101.assignment.global.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /*
        CustomException을 낚아채는 핸들러
    */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("[CustomException] {}: {}", errorCode.getCode(), errorCode.getMessage());

        return ErrorResponse.toResponseEntity(errorCode);
    }

    /*
         미처 잡지 못한 진짜 찐 에러 (NullPointerException 등) 낚아채기
    */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception e) {
        // 예상치 못한 에러는 상세 스택 트레이스까지 로그로 남겨서 고쳐야 함
        log.error("[UnhandledException] 예상치 못한 에러 발생", e);

        // 프론트에겐 서버 내부 오류(500)라는 공통 규격으로 응답
        return ErrorResponse.toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    /*
        @Valid 또는 @Validated 바인딩 에러 처리
    */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        // 발생한 모든 필드 에러를 싹 다 가져와서 우리 규격(ValidationError)으로 변환
        List<ErrorResponse.ValidationError> validationErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ErrorResponse.ValidationError::from)
                .toList();

        // 에러 응답 객체 조립 (상태 코드는 400, 에러 코드는 C001, 그리고 상세 에러 리스트 포함)
        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, validationErrors);

        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(body);
    }

    /*
        @RequestParam, @PathVariable 바인딩 에러 처리
    */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        // 단일 파라미터 에러들을 ValidationError으로 변환
        List<ErrorResponse.ValidationError> validationErrors = e.getConstraintViolations().stream()
                .map(violation -> {
                    // 예: "getItem.id" 형태로 나오기 때문에 마지막 "id"만 쏙 빼내는 작업
                    String propertyPath = violation.getPropertyPath().toString();
                    String field = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
                    return new ErrorResponse.ValidationError(field, violation.getMessage());
                })
                .toList();

        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, validationErrors);

        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(body);

    }
}
