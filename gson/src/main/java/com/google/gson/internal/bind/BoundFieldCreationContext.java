package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class BoundFieldCreationContext{
    final Gson context;
    final Field field;
    final Method accessor;
    final String serializedName;
    final TypeToken<?> fieldType;

    BoundFieldCreationContext(
        final Gson context,
        final Field field,
        final Method accessor,
        final String serializedName,
        final TypeToken<?> fieldType){

        this.context = context;
        this.field = field;
        this.accessor = accessor;
        this.serializedName = serializedName;
        this.fieldType = fieldType;

    }
}