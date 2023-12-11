package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
    gson = new GsonBuilder().registerTypeAdapterFactory(new ResolveGenericBoundFactory()).create();
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

  static class ResolveGenericBoundFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getType() instanceof TypeVariable<?>) {
        TypeVariable<?> tv = (TypeVariable<?>) type.getType();
        Type[] bounds = tv.getBounds();
        if (bounds.length == 1 && bounds[0] != Object.class) {
          Type bound = bounds[0];
          return (TypeAdapter<T>) gson.getAdapter(TypeToken.get(bound));
        }
      }
      return null;
    }
  }

  @Test
  public void testSubClassSerialization() {
    BarDynamic<Foo> bar = new BarDynamic<>(new Foo("foo!"));
    assertThat(gson.toJson(bar)).isEqualTo("{\"foo\":{\"text\":\"foo!\"}}");
    // without #2563 fix, this would deserialize foo as Object and fails to assign it to foo field
    BarDynamic<?> deserialized = gson.fromJson(gson.toJson(bar), BarDynamic.class);
    assertThat(deserialized.getFoo().getText()).isEqualTo("foo!");
  }
}
