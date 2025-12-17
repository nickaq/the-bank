package com.thebank.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for Swagger documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TheBank API")
                        .description("""
                                REST API for TheBank - a banking system with:
                                - Customer management
                                - EUR accounts
                                - Internal transfers
                                - Double-entry ledger
                                - Audit logging
                                
                                ## Authentication
                                Use Bearer token authentication. Get token via `/api/v1/auth/login`.
                                
                                ## Roles
                                - **CLIENT**: View own data, make transfers
                                - **ADMIN**: Full access to all operations
                                - **AUDITOR**: Read-only access to audit logs
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TheBank Team")
                                .email("support@thebank.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")));
    }
}
