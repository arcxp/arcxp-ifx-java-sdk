package com.arcxp.platform.sdk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to define a class an async event handler.
 */

@Component
@Inherited
@Target({ElementType.TYPE})
@Retention(RUNTIME)
public @interface ArcAsyncEvent {
    String[] value();
}
