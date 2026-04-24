package com.github.jhh0101.assignment.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Course
    COURSE_INVALID_PERIOD(HttpStatus.BAD_REQUEST, "COURSE001", "종료일은 시작일보다 빠를 수 없습니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE002", "강의를 찾을 수 없습니다."),
    COURSE_STATUS_CHANGE_ERROR(HttpStatus.BAD_REQUEST, "COURSE003", "모집 종료된 강의는 수정할 수 없습니다."),
    INVALID_CAPACITY_UPDATE(HttpStatus.BAD_REQUEST, "COURSE004", "최대 인원은 현재 신청 인원보다 많아야 합니다."),
    NOT_OPEN_COURSE(HttpStatus.BAD_REQUEST, "COURSE005", "현재 강의는 수강 신청 기간이 아닙니다."),
    ALREADY_ENROLLED(HttpStatus.BAD_REQUEST, "COURSE006", "이미 수강 신청한 강의입니다."),
    CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "COURSE007", "수강 신청 인원이 초과되었습니다."),
    INVALID_STATUS_UPDATE(HttpStatus.BAD_REQUEST, "COURSE008", "수강 시작일은 오늘(현재) 날짜보다 최소 2일 이후로 설정해야 합니다."),

    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ENROLLMENT001", "해당 수강 신청 정보를 찾을 수 없습니다."),
    ENROLLMENT_NOT_PENDING(HttpStatus.BAD_REQUEST, "ENROLLMENT002", "해당 강의는 이미 신청하거나 취소된 상태입니다."),
    ENROLLMENT_IS_CANCELLED(HttpStatus.BAD_REQUEST, "ENROLLMENT003", "해당 강의는 이미 취소된 상태입니다."),
    REFUND_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "ENROLLMENT004", "환불 유효 기간이 지났습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "U002", "사용자가 일치하지 않습니다."),
    USER_NOT_CREATOR(HttpStatus.FORBIDDEN, "U003", "사용자가 CREATOR 권한이 없습니다."),
    NOT_COURSE_OWNER(HttpStatus.FORBIDDEN, "U004", "강사가 일치하지 않습니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력 값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S999", "서버 내부 오류가 발생했습니다.");

    /*
        400 (Bad Request): 클라이언트 탓. (입력값 오류, 이메일 형식 틀림, 비밀번호 짧음)
        401 (Unauthorized): 인증 안 됨. (로그인 안 함, 토큰 만료됨, 토큰 이상함)
        403 (Forbidden): 권한 없음. (로그인은 했는데, 일반 유저가 관리자 페이지 접근 시도)
        404 (Not Found): 데이터 없음. (없는 회원 조회, 이미 삭제된 경매 상품 조회)
        409 (Conflict): 데이터 충돌. (이미 가입된 이메일로 또 가입 시도, 중복 닉네임)
        500 (Internal Server Error): 서버(내) 탓. (DB 연결 끊김, NullPointerException 등 내가 코드 잘못 짠 경우)
    */

    private final HttpStatus status;
    private final String code;
    private final String message;
}
