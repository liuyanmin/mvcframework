package com.lym.mvc.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @ClassName LAutowired
 * @Description
 * @Author LYM
 * @Date 2019/4/16 15:31
 * @Version 1.0.0
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LAutowired {

    String value() default "";
}
