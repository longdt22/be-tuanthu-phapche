package com.nms.evaluation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Checklist Service API")
                        .version("1.0.0")
                        .description("API Documentation for NMS Checklist Service. Built with Spring Boot 3.2.11 and Java 21.")
                        .contact(new Contact()
                                .name("NMS Development Team")
                                .email("dev@nms.com")
                                .url("https://nms.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
