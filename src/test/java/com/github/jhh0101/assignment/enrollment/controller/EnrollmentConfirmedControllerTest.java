package com.github.jhh0101.assignment.enrollment.controller;

import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.course.dto.CourseEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.user.dto.UserEnrollmentResponse;
import com.github.jhh0101.assignment.domain.enrollment.controller.EnrollmentController;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentConfirmedResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.service.EnrollmentService;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
public class EnrollmentConfirmedControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EnrollmentService enrollmentService;

    @MockitoBean
    private CourseEnrollmentClient courseClient;

    @MockitoBean
    private UserEnrollmentClient userClient;

    @Test
    @DisplayName("결제 확정 성공 테스트")
    void enrollmentConfirmed_success() throws Exception {
        Long userId = 1L;
        Long enrollmentId = 1L;

        EnrollmentConfirmedResponse response = EnrollmentConfirmedResponse.builder()
                .id(1L)
                .name("Test Name")
                .title("Test Title")
                .status(EnrollmentStatus.CONFIRMED) // 🌟 여기서 CONFIRMED로 설정!
                .enrolledAt(LocalDateTime.now())    // 🌟 여기서 날짜도 넣어주기!
                .build();

        given(enrollmentService.enrollmentConfirmed(userId, enrollmentId))
                .willReturn(response);

        mockMvc.perform(patch("/api/enrollment/confirmed/{enrollmentId}", 1L)
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("C000"))
                .andExpect(jsonPath("$.message").value("강의 결제 확정 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Test Title"))
                .andExpect(jsonPath("$.data.name").value("Test Name"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.enrolledAt").exists());

        then(enrollmentService).should(times(1)).enrollmentConfirmed(eq(1L), eq(1L));
    }

    @Test
    @DisplayName("결제 확정 실패 테스트 - 수강 정보를 찾을 수 없음")
    void enrollmentConfirmed_enrollment_not_found() throws Exception {
        willThrow(new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND))
                .given(enrollmentService).enrollmentConfirmed(anyLong(), anyLong());

        mockMvc.perform(patch("/api/enrollment/confirmed/{enrollmentId}", 1L)
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ENROLLMENT001"));
    }

    @Test
    @DisplayName("결제 확정 실패 테스트 - 사용자 정보가 일치하지 않음")
    void enrollmentConfirmed_user_forbidden_access() throws Exception {
        willThrow(new CustomException(ErrorCode.USER_FORBIDDEN_ACCESS))
                .given(enrollmentService).enrollmentConfirmed(anyLong(), anyLong());

        mockMvc.perform(patch("/api/enrollment/confirmed/{enrollmentId}", 1L)
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("U002"));
    }

    @Test
    @DisplayName("결제 확정 실패 테스트 - 해당 강의를 이미 신청하거나 취소된 상태")
    void enrollmentConfirmed_not_pending() throws Exception {
        willThrow(new CustomException(ErrorCode.ENROLLMENT_NOT_PENDING))
                .given(enrollmentService).enrollmentConfirmed(anyLong(), anyLong());

        mockMvc.perform(patch("/api/enrollment/confirmed/{enrollmentId}", 1L)
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ENROLLMENT002"));
    }
}