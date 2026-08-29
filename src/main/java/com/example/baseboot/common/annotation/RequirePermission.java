package com.example.baseboot.common.annotation;

import java.lang.annotation.*;

/**
 * 运营端管理员操作权限校验注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 权限标识 (如: "order:deliver", "shop:audit", 默认空表示仅需具备管理员身份)
     */
    String value() default "";
}
