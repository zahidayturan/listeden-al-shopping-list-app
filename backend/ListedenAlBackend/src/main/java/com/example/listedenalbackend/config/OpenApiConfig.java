package com.example.listedenalbackend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI listedenAlOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Listeden Al API")
                        .description("Alışveriş listeleri ve paylaşımı için Spring Boot Backend API")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Listeden Al Projesi GitHub Deposu")
                        .url("https://github.com/zahidayturan"));
    }
}