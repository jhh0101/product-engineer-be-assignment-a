package com.github.jhh0101.assignment.course.controller;

import com.github.jhh0101.assignment.domain.course.controller.CourseController;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.dto.CourseUpdateRequest;
import com.github.jhh0101.assignment.domain.course.entity.Course;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.repository.CourseListCondition;
import com.github.jhh0101.assignment.domain.course.repository.CourseRepository;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
public class CourseListControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CourseService courseService;

    @Test
    @DisplayName("강의 리스트 조회 성공 테스트")
    void courseList_success() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CourseResponse response1 = new CourseResponse(
                1L,
                "테스트 제목",
                "Test Description",
                250000,
                50,
                0,
                now,
                now.plusMonths(5),
                CourseStatus.DRAFT
        );
        CourseResponse response2 = new CourseResponse(
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
        CourseResponse response3 = new CourseResponse(
                1L,
                "테스트 제목",
                "Test Description",
                250000,
                50,
                0,
                now,
                now.plusMonths(5),
                CourseStatus.CLOSED
        );

        List<CourseResponse> content = List.of(response1, response2, response3);
        Pageable pageable = PageRequest.of(0, 10);
        Page<CourseResponse> pageResponse = new PageImpl<>(content, pageable, content.size());

        given(courseService.courseList(any(CourseListCondition.class), any(Pageable.class)))
                .willReturn(pageResponse);

        mockMvc.perform(get("/api/course")
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
                .andExpect(jsonPath("$.message").value("강의 리스트 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.data.content[1].status").value("OPEN"))
                .andExpect(jsonPath("$.data.content[2].status").value("CLOSED"))
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.content").isNotEmpty());

        then(courseService).should(times(1)).courseList(any(CourseListCondition.class), any(Pageable.class));
    }

    @Test
    @DisplayName("강의 리스트 조회 성공 테스트 - 리스트 조회 결과가 없을 때")
    void courseList_isEmpty() throws Exception {

        List<CourseResponse> content = List.of();
        Pageable pageable = PageRequest.of(0, 10);
        Page<CourseResponse> pageResponse = new PageImpl<>(content, pageable, content.size());

        given(courseService.courseList(any(CourseListCondition.class), any(Pageable.class)))
                .willReturn(pageResponse);

        mockMvc.perform(get("/api/course")
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
                .andExpect(jsonPath("$.message").value("강의 리스트 조회 성공"))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.empty").value(true))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

}
