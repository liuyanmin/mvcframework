package com.lym.mvc.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @ClassName RequestParam
 * @Description
 * @Author LYM
 * @Date 2019/4/16 15:34
 * @Version 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LRequestParam {

    String value() default "";
}
