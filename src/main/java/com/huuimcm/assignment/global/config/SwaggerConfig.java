package com.huuimcm.assignment.global.config;

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
                        .title("휴이엠컴퍼니 채용 과제 API")
                        .description("이커머스 과제 API 문서입니다. 모든 API는 /api/v1 prefix를 사용합니다.")
                        .version("v1"));
    }
}
