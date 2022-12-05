package com.arcxp.platform.sdk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to define a class an event handler.
 *
 * @deprecated This annotation has been replaced by {@link ArcAsyncEvent}.
 */

@Component
@Inherited
@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Deprecated
public @interface ArcEvent {
    String[] value();
}
