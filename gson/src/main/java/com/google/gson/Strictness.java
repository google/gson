package com.google.gson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Modes that indicate how strictly a JSON {@linkplain JsonReader reader} or {@linkplain JsonWriter
 * writer} follows the syntax laid out in the <a href="https://www.ietf.org/rfc/rfc8259.txt">RFC
 * 8259 JSON specification</a>.
 *
 * <p>You can look at {@link JsonReader#setStrictness(Strictness)} to see how the strictness affects
 * the {@link JsonReader} and you can look at {@link JsonWriter#setStrictness(Strictness)} to see
 * how the strictness affects the {@link JsonWriter}.
 *
 * @see GsonBuilder#setStrictness(Strictness)
 * @see JsonReader#setStrictness(Strictness)
 * @see JsonWriter#setStrictness(Strictness)
 * @since 2.11.0
 */
public enum Strictness {
  /** Allow large deviations from the JSON specification. */
  LENIENT,

  /** Allow certain small deviations from the JSON specification for legacy reasons. */
  LEGACY_STRICT,

  /** Strict compliance with the JSON specification. */
  STRICT
}
