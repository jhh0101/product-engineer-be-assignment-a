package com.github.jhh0101.assignment.course.controller;

import com.github.jhh0101.assignment.domain.course.controller.CourseController;
import com.github.jhh0101.assignment.domain.course.dto.CourseCreateRequest;
import com.github.jhh0101.assignment.domain.course.dto.CourseDetailResponse;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
public class CourseDetailControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("강의 상세 조회 성공 테스트")
    void courseDetail_success() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CourseDetailResponse response = new CourseDetailResponse(
                1L,
                "Test Title",
                "Test Description",
                150000,
                50,
                20,
                now,
                now.plusMonths(5),
                CourseStatus.DRAFT,
                "test Creator"
        );

        given(courseService.courseDetail(any(Long.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/course/{courseId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("C000"))
                .andExpect(jsonPath("$.message").value("강의 상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Test Title"))
                .andExpect(jsonPath("$.data.currentCapacity").value(20));

        then(courseService).should(times(1)).courseDetail(any(Long.class));
    }

    @Test
    @DisplayName("강의 상세 조회 실패 테스트 - 강의를 찾을 수 없음")
    void courseDetail_not_found() throws Exception {
        willThrow(new CustomException(ErrorCode.COURSE_NOT_FOUND))
                .given(courseService).courseDetail(any(Long.class));

        mockMvc.perform(get("/api/course/{courseId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COURSE002"));
    }
}
