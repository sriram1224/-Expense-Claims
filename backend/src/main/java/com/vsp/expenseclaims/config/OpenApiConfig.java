package com.vsp.expenseclaims.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Expense Claims Management API")
                        .version("1.0.0")
                        .description("Action-driven REST API for Expense Claims Management System. Supports Staff submission, Manager approvals (with self-approval guardrails), Finance payouts, smart receipt parsing, and duplicate detection.")
                        .contact(new Contact().name("VSP Engineering Team")));
    }

    @Bean
    public OperationCustomizer customGlobalHeaders() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new HeaderParameter()
                    .name("X-User-Id")
                    .description("ID of the user to act as (e.g., 1 for Rahul (Manager), 3 for Anita (Finance), 4 for Ram (Staff))")
                    .required(false));
            operation.addParametersItem(new HeaderParameter()
                    .name("X-Role")
                    .description("Role override (STAFF, MANAGER, FINANCE)")
                    .required(false));
            return operation;
        };
    }
}
