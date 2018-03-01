package com.google.gson.annotations;

import com.google.gson.GsonBuilder;

import java.lang.annotation.*;

/**
 * An annotation that indicates this member should be ignored for JSON
 * serialization or deserialization.
 *
 * <p>This annotation has no effect unless you build {@link com.google.gson.Gson}
 * with a {@link com.google.gson.GsonBuilder} and invoke
 * {@link GsonBuilder#excludeFieldsWithIgnoreAnnotation()} ()} method.
 * </p>
 *
 * <p> An example:
 * <p><pre>
 * public class Account {
 *   private String accountName;
 *   &#64Ignore(deserialize = false) private String info;
 *   &#64Ignore private String password;
 * }
 * </pre></p>
 * </p>
 *
 * If you create Gson via {@code new Gson()}, the {@code toJson()} and {@code fromJson()}
 * methods will use all fields for serialization and deserialization. However, you can create
 * Gson via {@code new GsonBuilder().excludeFieldsWithIgnoreAnnotation().create()}, then the
 * {@code toJson()} will exclude the {@code info} field and the {@code password} field,
 * because those fields were marked with the {@code @Ignore} annotation and set
 * {@code serialize = true}. Similarly, the {@code fromJson()} will exclude {@code password} since
 * {@code deserialize} is set to false.
 *
 * What's the difference from {@link Expose} annotation?
 * The {@link Expose} annotation indicates this member should be exposed for JSON serialization
 * or deserialization, it means all the fields that you wanna expose, you should be marked with
 * {@link Expose}:
 *
 * <p><pre>
 * public class Student {
 *   &#64Expose private Account account;
 *   &#64Expose private String class;
 * }
 * </pre></p>
 *
 * You can Create Gson with {@code Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()},
 * but the fields of {@code Account} class will be excluded because they are not marked by {@link Expose}.
 *
 * @author Rao-Mengnan
 *         on 2018/3/1.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Ignore {
    /**
     * If {@code true}, the field marked with this annotation is skipped from the
     * serialized output, otherwise the field marked with this annotation is written out in the JSON while
     * serializing.
     * Defaults to {@code true}.
     *
     * @since 2.8
     */
    public boolean serialize() default true;

    /**
     * If {@code true}, the field marked with this annotation will be skipped during deserialization.
     * If {@code false}, the field marked with this annotation will be deserialize from the JSON.
     * Defaults to {@code true}.
     *
     * @since 2.8
     */
    public boolean deserialize() default true;
}
