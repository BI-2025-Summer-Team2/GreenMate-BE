package kr.bi.greenmate.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiDocsConfig {
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(createApiInfo());
    }

    private Info createApiInfo() {
        return new Info()
                .title("GreenMate API")
                .description("GreenMate API 문서입니다.")
                .version("1.0.0");
    }
}
