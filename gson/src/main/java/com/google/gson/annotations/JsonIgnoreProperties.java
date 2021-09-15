package com.google.gson.annotations;

import java.lang.annotation.*;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-11-09
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonIgnoreProperties {

    /**
     * Names of properties to ignore.
     */
    public String[] value() default {};

}
