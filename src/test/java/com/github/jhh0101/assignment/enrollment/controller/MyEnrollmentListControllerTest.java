package com.github.jhh0101.assignment.enrollment.controller;

import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.repository.CourseListCondition;
import com.github.jhh0101.assignment.domain.enrollment.client.course.CourseEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.client.user.UserEnrollmentClient;
import com.github.jhh0101.assignment.domain.enrollment.controller.EnrollmentController;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentCancelledResponse;
import com.github.jhh0101.assignment.domain.enrollment.dto.EnrollmentListResponse;
import com.github.jhh0101.assignment.domain.enrollment.entity.EnrollmentStatus;
import com.github.jhh0101.assignment.domain.enrollment.service.EnrollmentService;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
public class MyEnrollmentListControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EnrollmentService enrollmentService;

    @MockitoBean
    private CourseEnrollmentClient courseClient;

    @MockitoBean
    private UserEnrollmentClient userClient;

    @Test
    @DisplayName("내 수강 신청 목록 조회 성공 테스트")
    void myEnrollmentList_success() throws Exception {
        Long userId = 1L;

        EnrollmentListResponse response1 = EnrollmentListResponse.builder()
                .id(1L)
                .name("Test Name")
                .title("Test Title1")
                .status(EnrollmentStatus.PENDING)
                .enrolledAt(LocalDateTime.now())
                .build();
        EnrollmentListResponse response2 = EnrollmentListResponse.builder()
                .id(2L)
                .name("Test Name")
                .title("Test Title2")
                .status(EnrollmentStatus.CONFIRMED)
                .enrolledAt(LocalDateTime.now())
                .build();
        EnrollmentListResponse response3 = EnrollmentListResponse.builder()
                .id(3L)
                .name("Test Name")
                .title("Test Title3")
                .status(EnrollmentStatus.CANCELLED)
                .enrolledAt(LocalDateTime.now())
                .build();

        List<EnrollmentListResponse> responses = List.of(response1, response2, response3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<EnrollmentListResponse> pageResponse = new PageImpl<>(responses, pageable, responses.size());

        given(enrollmentService.myEnrollmentList(eq(userId), any(Pageable.class)))
                .willReturn(pageResponse);

        mockMvc.perform(get("/api/enrollment/my/list")
                        .param("userId", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("C000"))
                .andExpect(jsonPath("$.message").value("내 신청 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.content[1].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.content[2].status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.content").isNotEmpty());

        then(enrollmentService).should(times(1)).myEnrollmentList(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("내 수강 신청 목록 조회 성공 테스트 - 수강 내역이 없는 경우 (빈 페이지)")
    void myEnrollmentList_success_empty() throws Exception {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Page<EnrollmentListResponse> emptyPage = Page.empty(pageable);

        given(enrollmentService.myEnrollmentList(eq(userId), any(Pageable.class)))
                .willReturn(emptyPage);

        mockMvc.perform(get("/api/enrollment/my/list")
                        .param("userId", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("C000"))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("내 수강 신청 목록 조회 실패 테스트 - 강의를 찾을 수 없음")
    void myEnrollmentList_course_not_found() throws Exception {
        willThrow(new CustomException(ErrorCode.COURSE_NOT_FOUND))
                .given(enrollmentService).myEnrollmentList(anyLong(), any(Pageable.class));

        mockMvc.perform(get("/api/enrollment/my/list")
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
    @DisplayName("내 수강 신청 목록 조회 실패 테스트 - 사용자를 찾을 수 없음")
    void myEnrollmentList_user_not_found() throws Exception {
        willThrow(new CustomException(ErrorCode.USER_NOT_FOUND))
                .given(enrollmentService).myEnrollmentList(anyLong(), any(Pageable.class));

        mockMvc.perform(get("/api/enrollment/my/list")
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
}