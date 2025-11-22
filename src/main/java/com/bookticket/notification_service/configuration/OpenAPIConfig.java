package com.bookticket.notification_service.configuration;

import com.bookticket.notification_service.configuration.swagger.InternalApi;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class OpenAPIConfig {

    private static final String SCHEME_NAME = "bearerAuth";
    private static final String SCHEME = "bearer";
    private static final String BEARER_FORMAT = "JWT";
    private static final String API_GATEWAY_URL = "http://localhost:8080";

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("notification-service")
                .packagesToScan("com.bookticket.notification_service")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url(API_GATEWAY_URL))
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(SCHEME)
                                        .bearerFormat(BEARER_FORMAT)));
    }

    @Bean
    public OperationCustomizer internalApiCustomizer() {
        return (operation, handlerMethod) -> {
            InternalApi internalApi = handlerMethod.getMethodAnnotation(InternalApi.class);
            if (internalApi != null) {
                operation.addExtension("x-try-it-out-enabled", false);
                operation.setSummary(operation.getSummary() + " (Internal API)");
            }
            return operation;
        };
    }

    private Info apiInfo() {
        return new Info()
                .title("Notification Service API")
                .description("""
                        <h2>Notification Service API Documentation</h2>
                        <p>This service handles sending notifications, such as email and SMS, to users.</p>
                        """)
                .version("1.0")
                .contact(new Contact()
                        .name("BookTicket Support")
                        .email("bookticket.com@gmail.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }
}
