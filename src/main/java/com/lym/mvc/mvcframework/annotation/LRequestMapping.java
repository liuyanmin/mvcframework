package com.lym.mvc.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @ClassName LRequestMapping
 * @Description
 * @Author LYM
 * @Date 2019/4/16 15:32
 * @Version 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LRequestMapping {

    String value() default "";
}
