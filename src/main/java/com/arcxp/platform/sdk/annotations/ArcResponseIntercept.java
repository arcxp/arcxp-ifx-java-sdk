package com.arcxp.platform.sdk.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to define a class a response interceptor handler.
 *
 * @deprecated As of release 2.0.0, this annotation is no longer used for a response interceptor handler.
 *     <p>Use {@link ArcSyncEvent} instead.</p>
 */

@Component
@Inherited
@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Deprecated
public @interface ArcResponseIntercept {
    String[] value();
}
