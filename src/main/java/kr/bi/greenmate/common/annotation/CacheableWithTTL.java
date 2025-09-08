package kr.bi.greenmate.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheableWithTTL {
    String cacheName();

    long ttl();

    ChronoUnit unit() default ChronoUnit.SECONDS;
}
