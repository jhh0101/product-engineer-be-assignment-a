package com.github.jhh0101.assignment.enrollment.controller;

import com.github.jhh0101.assignment.domain.enrollment.controller.EnrollmentController;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentRegistrationResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.service.EnrollmentService;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
public class CourseRegistrationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EnrollmentService enrollmentService;

    @Test
    @DisplayName("수강 신청 성공 테스트")
    void courseRegistration_success() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        EnrollmentRegistrationResponse response = new EnrollmentRegistrationResponse(
                1L,
                "Test User",
                "Test Title",
                EnrollmentStatus.PENDING,
                now
        );

        given(enrollmentService.courseRegistration(anyLong(), anyLong()))
                .willReturn(response);

        mockMvc.perform(post("/api/enrollment/{courseId}", 1L)
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("C000"))
                .andExpect(jsonPath("$.message").value("수강 신청 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Test Title"));

        then(enrollmentService).should(times(1)).courseRegistration(anyLong(), anyLong());
    }

    @Test
    @DisplayName("수강 신청 실패 테스트 - 강의를 찾을 수 없음")
    void courseRegistration_course_not_found() throws Exception {
        willThrow(new CustomException(ErrorCode.COURSE_NOT_FOUND))
                .given(enrollmentService).courseRegistration(anyLong(), anyLong());

        mockMvc.perform(post("/api/enrollment/{courseId}", 1L)
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COURSE002"));
    }

    @Test
    @DisplayName("수강 신청 실패 테스트 - 사용자를 찾을 수 없음")
    void courseRegistration_user_not_found() throws Exception {
        willThrow(new CustomException(ErrorCode.USER_NOT_FOUND))
                .given(enrollmentService).courseRegistration(anyLong(), anyLong());

        mockMvc.perform(post("/api/enrollment/{courseId}", 1L)
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("U001"));
    }

    @Test
    @DisplayName("수강 신청 실패 테스트 - 강의 중복 신청 불가능")
    void courseRegistration_already_enrolled() throws Exception {
        willThrow(new CustomException(ErrorCode.ALREADY_ENROLLED))
                .given(enrollmentService).courseRegistration(anyLong(), anyLong());

        mockMvc.perform(post("/api/enrollment/{courseId}", 1L)
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COURSE006"));
    }

    @Test
    @DisplayName("수강 신청 실패 테스트 - 강의 상태가 Open이 아님")
    void courseRegistration_course_not_open() throws Exception {
        willThrow(new CustomException(ErrorCode.NOT_OPEN_COURSE))
                .given(enrollmentService).courseRegistration(anyLong(), anyLong());

        mockMvc.perform(post("/api/enrollment/{courseId}", 1L)
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COURSE005"));
    }

    @Test
    @DisplayName("수강 신청 실패 테스트 - 수강 신청 인원이 초과 됨")
    void courseRegistration_course_capacity_exceeded() throws Exception {
        willThrow(new CustomException(ErrorCode.CAPACITY_EXCEEDED))
                .given(enrollmentService).courseRegistration(anyLong(), anyLong());

        mockMvc.perform(post("/api/enrollment/{courseId}", 1L)
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COURSE007"));
    }
}