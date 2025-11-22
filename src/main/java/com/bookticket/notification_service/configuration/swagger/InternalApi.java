package com.bookticket.notification_service.configuration.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark an endpoint as internal.
 * This will be used by the OpenApiCustomiser to disable the "Try it out" button in Swagger UI.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalApi {
}
