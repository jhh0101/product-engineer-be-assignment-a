package com.github.jhh0101.assignment.global.config;

import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.domain.course.service.CourseSyncService;
import com.github.jhh0101.assignment.domain.user.entity.Role;
import com.github.jhh0101.assignment.domain.user.entity.User;
import com.github.jhh0101.assignment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("prod") // 도커 환경에서만 실행되도록 설정 (테스트 시 방해 금지)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseSyncService courseSyncService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        User creator = User.builder().name("테스트강사").role(Role.CREATOR).build();
        userRepository.save(creator);

        for (int i = 1; i <= 5; i++) {
            userRepository.save(
                    User.builder()
                            .name("수강생" + i)
                            .role(Role.USER)
                            .build()
            );
        }

        Course course = Course.builder()
                .creatorId(1L)
                .title("실시간 선착순 수강신청 실전")
                .price(50000)
                .maxCapacity(3)
                .startTime(LocalDateTime.now().plusDays(1).plusMinutes(5))
                .endTime(LocalDateTime.now().plusDays(30))
                .status(CourseStatus.OPEN)
                .build();
        courseRepository.save(course);

        courseSyncService.syncRedisWithDatabase();
        System.out.println("초기 데이터 세팅 완료 (강사 1명, 학생 5명, 강의 1개)");
    }
}