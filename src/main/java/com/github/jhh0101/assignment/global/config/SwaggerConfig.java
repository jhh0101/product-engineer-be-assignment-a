package com.github.jhh0101.assignment.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("수강 신청 시스템 API")
                        .description("수강 신청 및 결제, 동시성 제어를 위한 API 명세서입니다.")
                        .version("v1.0.0"));
    }
}