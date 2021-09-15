package com.google.gson;

import com.google.gson.annotations.JsonIgnoreProperties;
import com.google.gson.internal.Excluder;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-11-09
 */
public class GsonExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(Class<?> rootClass, FieldAttributes f) {

        return Excluder.DEFAULT.excludeProperties(rootClass, f.getName());
    }


    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}