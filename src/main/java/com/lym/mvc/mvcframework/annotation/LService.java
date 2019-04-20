package com.lym.mvc.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @ClassName LService
 * @Description
 * @Author LYM
 * @Date 2019/4/16 15:30
 * @Version 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LService {

    String value() default "";
}
