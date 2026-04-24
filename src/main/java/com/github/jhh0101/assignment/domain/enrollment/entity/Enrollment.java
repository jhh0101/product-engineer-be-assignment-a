package com.github.jhh0101.assignment.domain.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "enrollment")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    @Version
    private Long version;

    public void enrollmentConfirmed() {
        this.enrolledAt = LocalDateTime.now();
        this.status = EnrollmentStatus.CONFIRMED;
    }

    public void enrollmentCancelled() {
        this.status = EnrollmentStatus.CANCELLED;
    }

    public void reEnroll() {
        this.status = EnrollmentStatus.PENDING;
    }
}
