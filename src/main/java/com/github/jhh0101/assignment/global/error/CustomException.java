package com.github.jhh0101.assignment.global.error;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    // 이 예외가 터질 때 무조건 우리가 만든 ErrorCode를 하나 품도록 강제합니다.
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());  // 예외 로그가 찍힐 때 내용을 알 수 있도록 부모(RuntimeException)에게도 메시지 전달
        this.errorCode = errorCode;
    }
}
