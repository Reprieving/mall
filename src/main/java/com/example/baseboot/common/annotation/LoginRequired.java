package com.example.baseboot.common.annotation;

import java.lang.annotation.*;

/**
 * 需要登录认证的注解
 * 可标记在类或方法上
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginRequired {
    boolean required() default true;
}
