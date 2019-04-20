package com.lym.mvc.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @ClassName LController
 * @Description
 * @Author LYM
 * @Date 2019/4/16 15:29
 * @Version 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LController {

    String value() default "";
}
