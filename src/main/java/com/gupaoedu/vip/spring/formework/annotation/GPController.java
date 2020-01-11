package com.gupaoedu.vip.spring.formework.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface GPController {
    String value() default "";
}