package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;

/**
 * Test processing raw enum types.
 *
 * @author sevcenko
 */
public class HandleRawEnumTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  public enum SomeEnum {
    ONE
  }

  public static class ClassWithRawEnum {
    private final Enum<?> anyEnum;

    public ClassWithRawEnum(Enum<?> anyEnum) {
      this.anyEnum = anyEnum;
    }

    public Enum<?> getAnyEnum() {
      return anyEnum;
    }
  }

  public static class ClassWithTypedEnum<T extends Enum<T>> {
    private final T someEnum;

    public ClassWithTypedEnum(T someEnum) {
      this.someEnum = someEnum;
    }

    public T getSomeEnum() {
      return someEnum;
    }
  }

  public static class GroupClass {

    private final ClassWithTypedEnum<SomeEnum> field;

    public GroupClass(ClassWithTypedEnum<SomeEnum> field) {
      this.field = field;
    }

    public ClassWithTypedEnum<SomeEnum> getField() {
      return field;
    }
  }

  @Test
  public void handleRawEnumClass() {
    // serializing raw enum is fine, but note that Adapters.ENUM_FACTORY cannot handle raw enums
    // even for serialization! before #2563, this just failed because raw enum falled through
    // ReflectiveTypeAdapterFactory, which fails to even search enum for fields
    assertThat(gson.toJson(new ClassWithRawEnum(SomeEnum.ONE))).isEqualTo("{\"anyEnum\":\"ONE\"}");

    // we can deserialize if the enum type is known
    assertThat(
            gson.fromJson(
                    "{\"someEnum\":\"ONE\"}", new TypeToken<ClassWithTypedEnum<SomeEnum>>() {})
                .getSomeEnum())
        .isEqualTo(SomeEnum.ONE);

    assertThat(gson.toJson(new GroupClass(new ClassWithTypedEnum<>(SomeEnum.ONE))))
        .isEqualTo("{\"field\":{\"someEnum\":\"ONE\"}}");

    assertThat(
            gson.fromJson("{\"field\":{\"someEnum\":\"ONE\"}}", GroupClass.class)
                .getField()
                .getSomeEnum())
        .isEqualTo(SomeEnum.ONE);
    ;
    try {
      //       but raw type cannot be deserialized
      gson.fromJson("{\"anyEnum\":\"ONE\"}", new TypeToken<ClassWithRawEnum>() {});
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().contains("Can not set final java.lang.Enum field");
    }
  }
}
