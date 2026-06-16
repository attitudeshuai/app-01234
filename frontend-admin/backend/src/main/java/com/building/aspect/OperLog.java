package com.building.aspect;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 模块名称 */
    String module() default "";

    /** 操作类型 */
    String action() default "";
}
