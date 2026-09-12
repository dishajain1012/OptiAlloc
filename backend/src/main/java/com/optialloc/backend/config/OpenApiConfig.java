package com.optialloc.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "OptiAlloc REST API",
                version = "1.0",
                description = "Full-stack Resource Allocation and Scheduling System REST API Documentation",
                contact = @Contact(name = "OptiAlloc Engineering", email = "support@optialloc.local")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter JWT Bearer Token obtained from POST /api/auth/login"
)
public class OpenApiConfig {
}
