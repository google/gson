package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

/**
 * Test deserialization of generic wrapper with type bound.
 * 
 * @author sevcenko
 */
public class InferenceFromTypeVariableTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  public static class Foo {
    private final String text;

    public Foo(String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }
  }

  public static class BarDynamic<T extends Foo> {
    private final T foo;

    public BarDynamic(T foo) {
      this.foo = foo;
    }

    public T getFoo() {
      return foo;
    }
  }

  @Test
  public void testGenericWrapperWithBoundDeserialization() {
    BarDynamic<Foo> bar = new BarDynamic<>(new Foo("foo!"));
    assertThat(gson.toJson(bar)).isEqualTo("{\"foo\":{\"text\":\"foo!\"}}");
    // without #2563 fix, this would deserialize foo as Object and fails to assign it to foo field
    BarDynamic<?> deserialized = gson.fromJson(gson.toJson(bar), BarDynamic.class);
    assertThat(deserialized.getFoo().getText()).isEqualTo("foo!");
  }
}
