package com.github.jhh0101.assignment.course.controller;

import com.github.jhh0101.assignment.domain.course.dto.CourseRequest;
import com.github.jhh0101.assignment.domain.course.dto.CourseResponse;
import com.github.jhh0101.assignment.domain.course.entity.CourseStatus;
import com.github.jhh0101.assignment.domain.course.service.CourseService;
import com.github.jhh0101.assignment.global.error.CustomException;
import com.github.jhh0101.assignment.global.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseCreateControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void courseCreate_success() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CourseRequest request = new CourseRequest(
                "Test Title",
                "Test Description",
                150000,
                50,
                now,
                now.plusMonths(5)
        );

        CourseResponse response = new CourseResponse(
                1L,
                "Test Title",
                "Test Description",
                150000,
                50,
                0,
                now,
                now.plusMonths(5),
                CourseStatus.DRAFT
        );

        given(courseService.courseCreate(any(CourseRequest.class)))
                .willReturn(response);

        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("C000"))
                .andExpect(jsonPath("$.message").value("강의 등록 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Test Title"));

        then(courseService).should(times(1)).courseCreate(any(CourseRequest.class));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDtos")
    void courseCreate_validDto(CourseRequest requestDtos) throws Exception {

        String body = objectMapper.writeValueAsString(requestDtos);

        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.validationErrors").exists());

        then(courseService).should(never()).courseCreate(any(CourseRequest.class));

    }

    static Stream<CourseRequest> provideInvalidDtos() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                new CourseRequest(
                        null,
                        "Test Description",
                        150000,
                        50,
                        now,
                        now.plusMonths(5)
                ),
                new CourseRequest(
                        "",
                        "Test Description",
                        150000,
                        50,
                        now,
                        now.plusMonths(5)
                ),
                new CourseRequest(
                        " ",
                        "Test Description",
                        -150000,
                        50,
                        now,
                        now.plusMonths(5)
                ),
                new CourseRequest(
                        "Test Title",
                        "Test Description",
                        -150000,
                        50,
                        now,
                        now.plusMonths(5)
                ),
                new CourseRequest(
                        "Test Title",
                        "Test Description",
                        150000,
                        0,
                        now,
                        now.plusMonths(5)
                ),
                new CourseRequest(
                        "Test Title",
                        "Test Description",
                        150000,
                        50,
                        null,
                        now.plusMonths(5)
                )
        );
    }

    @Test
    void courseCreate_startTime_Period() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CourseRequest request = new CourseRequest(
                "Test Title",
                "Test Description",
                150000,
                50,
                now,
                now.minusDays(5)
        );

        String body = objectMapper.writeValueAsString(request);

        willThrow(new CustomException(ErrorCode.COURSE_INVALID_PERIOD))
                .given(courseService).courseCreate(any(CourseRequest.class));

        mockMvc.perform(post("/api/course")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(result -> {
                    System.out.println("TEST 결과 : ");
                })
                .andDo(print())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COURSE001"));
    }
}
