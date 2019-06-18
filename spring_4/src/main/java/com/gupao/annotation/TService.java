package com.gupao.annotation;


import java.lang.annotation.*;

/**
 * 业务逻辑
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TService {

    String value() default "";

}
