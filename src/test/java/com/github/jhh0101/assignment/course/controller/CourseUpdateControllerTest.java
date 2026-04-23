package com.github.jhh0101.assignment.course.controller;

import com.github.jhh0101.assignment.domain.course.controller.CourseController;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.dto.CourseUpdateRequest;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
public class CourseUpdateControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("강의 수정 성공 테스트")
    void courseUpdate_success() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CourseUpdateRequest request = new CourseUpdateRequest(
                "테스트 제목",
                null,
                250000,
                50,
                now,
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        CourseResponse response = new CourseResponse(
                1L,
                "테스트 제목",
                "Test Description",
                250000,
                50,
                0,
                now,
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        given(courseService.courseUpdate(eq(1L), any(CourseUpdateRequest.class)))
                .willReturn(response);

        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/course/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("C000"))
                .andExpect(jsonPath("$.message").value("강의 수정 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.description").value("Test Description"))
                .andExpect(jsonPath("$.data.price").value(250000))
                .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        then(courseService).should(times(1)).courseUpdate(eq(1L), any(CourseUpdateRequest.class));
    }

    @Test
    @DisplayName("강의 수정 날짜 검증 에러 테스트")
    void courseUpdate_startTime_Period() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CourseUpdateRequest request = new CourseUpdateRequest(
                "테스트 제목",
                null,
                250000,
                50,
                now,
                now.minusDays(5),
                CourseStatus.OPEN
        );

        String body = objectMapper.writeValueAsString(request);

        willThrow(new CustomException(ErrorCode.COURSE_INVALID_PERIOD))
                .given(courseService).courseUpdate(eq(1L), any(CourseUpdateRequest.class));

        mockMvc.perform(patch("/api/course/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COURSE001"));
    }

    @Test
    @DisplayName("강의 데이터 조회 에러 테스트")
    void courseUpdate_not_found() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CourseUpdateRequest request = new CourseUpdateRequest(
                "테스트 제목",
                null,
                250000,
                50,
                now,
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        String body = objectMapper.writeValueAsString(request);

        willThrow(new CustomException(ErrorCode.COURSE_NOT_FOUND))
                .given(courseService).courseUpdate(eq(1L), any(CourseUpdateRequest.class));

        mockMvc.perform(patch("/api/course/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COURSE002"));
    }

    @Test
    @DisplayName("강의 상태 변경 에러 테스트")
    void courseUpdate_status_change_error() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CourseUpdateRequest request = new CourseUpdateRequest(
                "테스트 제목",
                null,
                250000,
                50,
                now,
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        String body = objectMapper.writeValueAsString(request);

        willThrow(new CustomException(ErrorCode.COURSE_STATUS_CHANGE_ERROR))
                .given(courseService).courseUpdate(eq(1L), any(CourseUpdateRequest.class));

        mockMvc.perform(patch("/api/course/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COURSE003"));
    }

    @Test
    @DisplayName("수강자 최대 인원 변경 에러 테스트")
    void courseUpdate_invalid_maxCapacity() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CourseUpdateRequest request = new CourseUpdateRequest(
                "테스트 제목",
                null,
                250000,
                30,
                now,
                now.plusMonths(5),
                CourseStatus.OPEN
        );

        String body = objectMapper.writeValueAsString(request);

        willThrow(new CustomException(ErrorCode.INVALID_CAPACITY_UPDATE))
                .given(courseService).courseUpdate(eq(1L), any(CourseUpdateRequest.class));

        mockMvc.perform(patch("/api/course/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COURSE004"));
    }
}
