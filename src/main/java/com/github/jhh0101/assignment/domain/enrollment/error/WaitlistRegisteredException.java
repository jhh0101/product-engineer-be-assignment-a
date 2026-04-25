package com.github.jhh0101.assignment.domain.enrollment.error;

import lombok.Getter;

@Getter
public class WaitlistRegisteredException extends RuntimeException {
    private final Long userId;
    private final String name;
    private final String title;
    private final Long waitNumber;
    private final Long totalWaitingCount;

    public WaitlistRegisteredException(Long userId, String name, String title, Long waitNumber, Long totalWaitingCount) {
        super("정원 초과로 대기열에 등록되었습니다. 대기 순번: " + waitNumber);
        this.userId = userId;
        this.name = name;
        this.title = title;
        this.waitNumber = waitNumber;
        this.totalWaitingCount = totalWaitingCount;
    }
}