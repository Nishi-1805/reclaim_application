package com.cdac.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    public static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI reclaimOpenAPI() {

        return new OpenAPI()

                .info(new Info()

                        .title("Reclaim API")

                        .version("1.0")

                        .description("Intelligent Lost and Found Matching & Ownership Verification Platform")

                        .contact(new Contact()
                                .name("CDAC Project")
                                .email("admin@reclaim.com"))

                        .license(new License()
                                .name("Apache 2.0")))

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME))

                .schemaRequirement(
                        SECURITY_SCHEME_NAME,

                        new SecurityScheme()

                                .name(SECURITY_SCHEME_NAME)

                                .type(SecurityScheme.Type.HTTP)

                                .scheme("bearer")

                                .bearerFormat("JWT"));
    }
}