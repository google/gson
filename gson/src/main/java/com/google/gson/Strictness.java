package com.google.gson;

/**
 * Modes that indicate how strictly a JSON {@linkplain com.google.gson.stream.JsonReader reader} or
 * {@linkplain com.google.gson.stream.JsonWriter writer} follows the syntax laid out in the
 * <a href="http://www.ietf.org/rfc/rfc8259.txt">RFC 8259 JSON specification</a>.
 *
 * <p>You can look at {@link com.google.gson.stream.JsonWriter#setStrictness(Strictness)} to see how the strictness
 * affects the {@link com.google.gson.stream.JsonWriter} and you can look at
 * {@link com.google.gson.stream.JsonReader#setStrictness(Strictness)} to see how the strictness
 * affects the {@link com.google.gson.stream.JsonReader} </p>
 *
 * @see com.google.gson.stream.JsonReader#setStrictness(Strictness)
 * @see com.google.gson.stream.JsonWriter#setStrictness(Strictness)
 */
public enum Strictness {
    LENIENT,
    DEFAULT,
    STRICT
}
