package com.google.gson.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * An annotation that indicates that upon deserialization {@code null} will be assigned
 * to an annotated field of declared type {@link JsonElement} or one of its subclasses
 * in case it has an explicit {@code null} value in JSON.
 *
 * <p>This annotation only has an effect if the JSON representation of the declaring
 * class of the annotated field is deserialized using Gson's reflection-based
 * deserialization approach (the default if no type adapter is specified for the class).
 *
 * <p>Normally {@link JsonElement} and its subclasses are deserialized as instance of
 * {@link JsonNull} if they have a JSON value of {@code null}. When the expected type
 * is {@code JsonElement}, this makes it cumbersome because one has to check for both
 * {@code null} and {@code JsonNull} after deserializing. And for subclasses of
 * {@code JsonElement}, such as {@link JsonObject}, an exception is thrown due to the
 * class mismatch.<br>
 * However, if the field is annotated with this annotation, no exception is thrown and
 * the value of the field will be {@code null}.
 *
 * <h1>Example</h1>
 * <pre>
 * public class Container {
 *    &#64;ExplicitlyNullableJsonElement private JsonObject attributes;
 *    &#64;ExplicitlyNullableJsonElement private JsonElement additionalData;
 *
 *    public boolean hasAdditionalData() {
 *      /*
 *       * Without &#64;ExplicitlyNullableJsonElement this check would have to be:
 *       *  - `additionalData != null`
 *       *    In case the property does not exist in JSON: `{}`
 *       *  - `!additionalData.isJsonNull()`
 *       *    In case the property has a `null` value in JSON:
 *       *    `{"additionalData": null}`
 *       *&#47;
 *      return additionalData != null;
 *    }
 * }
 * </pre>
 * Due to {@code @ExplicitlyNullableJsonElement} being present on the <i>attributes</i>
 * field, this allows explicit {@code null} values in JSON, which would otherwise
 * cause an exception:
 * <pre>
 * {
 *   "attributes" : null
 * }
 * </pre>
 *
 * @since 2.8.7
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface ExplicitlyNullableJsonElement {
}
