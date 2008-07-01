/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;

/**
 * Types used for testing JSON serialization and deserialization
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class TestTypes {

  public static class StringWrapper {
    public final String someConstantStringInstanceField;

    StringWrapper() {
      this("Blah");
    }

    public StringWrapper(String value) {
      someConstantStringInstanceField = value;
    }
  }

  public static class BagOfPrimitives {
    public static final long DEFAULT_VALUE = 0;
    public final long longValue;
    public final int intValue;
    public final boolean booleanValue;
    public final String stringValue;

    public BagOfPrimitives() {
      this(DEFAULT_VALUE, 0, false, "");
    }

    public BagOfPrimitives(long longValue, int intValue, boolean booleanValue, String stringValue) {
      this.longValue = longValue;
      this.intValue = intValue;
      this.booleanValue = booleanValue;
      this.stringValue = stringValue;
    }

    public int getIntValue() {
      return intValue;
    }

    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("\"longValue\":").append(longValue).append(",");
      sb.append("\"intValue\":").append(intValue).append(",");
      sb.append("\"booleanValue\":").append(booleanValue).append(",");
      sb.append("\"stringValue\":\"").append(stringValue).append("\"");
      sb.append("}");
      return sb.toString();
    }
  }

  public static class BagOfPrimitiveWrappers {
    private final Long longValue;
    private final Integer intValue;
    private final Boolean booleanValue;

    public BagOfPrimitiveWrappers() {
      this(0L, 0, false);
    }

    public BagOfPrimitiveWrappers(Long longValue, Integer intValue, Boolean booleanValue) {
      this.longValue = longValue;
      this.intValue = intValue;
      this.booleanValue = booleanValue;
    }

    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("\"longValue\":").append(longValue).append(",");
      sb.append("\"intValue\":").append(intValue).append(",");
      sb.append("\"booleanValue\":").append(booleanValue);
      sb.append("}");
      return sb.toString();
    }
  }

  public static class PrimitiveArray {
    private final long[] longArray;

    public PrimitiveArray() {
      this(new long[0]);
    }

    public PrimitiveArray(long[] longArray) {
      this.longArray = longArray;
    }

    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder();
      sb.append("{\"longArray\":[");

      boolean first = true;
      for (long l : longArray) {
        if (!first) {
          sb.append(",");
        } else {
          first = false;
        }
        sb.append(l);
      }

      sb.append("]}");
      return sb.toString();
    }
  }

  public static class Nested {
    private final BagOfPrimitives primitive1;
    private final BagOfPrimitives primitive2;

    public Nested() {
      this(null, null);
    }

    public Nested(BagOfPrimitives primitive1, BagOfPrimitives primitive2) {
      this.primitive1 = primitive1;
      this.primitive2 = primitive2;
    }

    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      appendFields(sb);
      sb.append("}");
      return sb.toString();
    }

    public void appendFields(StringBuilder sb) {
      if (primitive1 != null) {
        sb.append("\"primitive1\":").append(primitive1.getExpectedJson());
      }
      if (primitive1 != null && primitive2 != null) {
        sb.append(",");
      }
      if (primitive2 != null) {
        sb.append("\"primitive2\":").append(primitive2.getExpectedJson());
      }
    }
  }

  public static class ClassWithTransientFields {
    public final transient long transientLongValue;
    private final long[] longValue;

    public ClassWithTransientFields() {
      this(0L);
    }

    public ClassWithTransientFields(long value) {
      longValue = new long[] { value };
      transientLongValue = value + 1;
    }

    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("\"longValue\":[").append(longValue[0]).append("]");
      sb.append("}");
      return sb.toString();
    }
  }

  public static class ClassWithNoFields {
    // Nothing here.. .
  }

  public static class ClassWithSubInterfacesOfCollection {
    private List<Integer> list;
    private Queue<Long> queue;
    private Set<Float> set;
    private SortedSet<Character> sortedSet;

    ClassWithSubInterfacesOfCollection() {
    }

    public ClassWithSubInterfacesOfCollection(List<Integer> list, Queue<Long> queue, Set<Float> set,
        SortedSet<Character> sortedSet) {
      this.list = list;
      this.queue = queue;
      this.set = set;
      this.sortedSet = sortedSet;
    }

    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append("\"list\":");
      append(sb, list).append(",");
      sb.append("\"queue\":");
      append(sb, queue).append(",");
      sb.append("\"set\":");
      append(sb, set).append(",");
      sb.append("\"sortedSet\":");
      append(sb, sortedSet);
      sb.append("}");
      return sb.toString();
    }

    private StringBuilder append(StringBuilder sb, Collection<?> c) {
      sb.append("[");
      boolean first = true;
      for (Object o : c) {
        if (!first) {
          sb.append(",");
        } else {
          first = false;
        }
        if (o instanceof String || o instanceof Character) {
          sb.append('\"');
        }
        sb.append(o.toString());
        if (o instanceof String || o instanceof Character) {
          sb.append('\"');
        }
      }
      sb.append("]");
      return sb;
    }
  }

  public static class ContainsReferenceToSelfType {
    public Collection<ContainsReferenceToSelfType> children =
        new ArrayList<ContainsReferenceToSelfType>();
  }

  public static class SubTypeOfNested extends Nested {
    private final long value = 5;

    public SubTypeOfNested() {
      this(null, null);
    }

    public SubTypeOfNested(BagOfPrimitives primitive1, BagOfPrimitives primitive2) {
      super(primitive1, primitive2);
    }

    @Override
    public void appendFields(StringBuilder sb) {
      sb.append("\"value\":").append(value).append(",");
      super.appendFields(sb);
    }
  }

  public static class ClassWithCustomTypeConverter {
    private final BagOfPrimitives bag;
    private final int value;

    public ClassWithCustomTypeConverter() {
      this(new BagOfPrimitives(), 10);
    }

    public ClassWithCustomTypeConverter(int value) {
      this(new BagOfPrimitives(value, value, false, ""), value);
    }

    public ClassWithCustomTypeConverter(BagOfPrimitives bag, int value) {
      this.bag = bag;
      this.value = value;
    }

    public BagOfPrimitives getBag() {
      return bag;
    }

    public String getExpectedJson() {
      return "{\"url\":\"" + bag.getExpectedJson() + "\",\"value\":" + value + "}";
    }
    public int getValue() {
      return value;
    }
  }

  public static class ArrayOfObjects {
    private final BagOfPrimitives[] elements;
    public ArrayOfObjects() {
      elements = new BagOfPrimitives[3];
      for (int i = 0; i < elements.length; ++i) {
        elements[i] = new BagOfPrimitives(i, i+2, false, "i"+i);
      }
    }
    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder("{\"elements\":[");
      boolean first = true;
      for (BagOfPrimitives element : elements) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        sb.append(element.getExpectedJson());
      }
      sb.append("]}");
      return sb.toString();
    }
  }

  public static class ArrayOfArrays {
    private final BagOfPrimitives[][] elements;
    public ArrayOfArrays() {
      elements = new BagOfPrimitives[3][2];
      for (int i = 0; i < elements.length; ++i) {
        BagOfPrimitives[] row = elements[i];
        for (int j = 0; j < row.length; ++j) {
          row[j] = new BagOfPrimitives(i+j, i*j, false, i+"_"+j);
        }
      }
    }
    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder("{\"elements\":[");
      boolean first = true;
      for (BagOfPrimitives[] row : elements) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        boolean firstOfRow = true;
        sb.append("[");
        for (BagOfPrimitives element : row) {
          if (firstOfRow) {
            firstOfRow = false;
          } else {
            sb.append(",");
          }
          sb.append(element.getExpectedJson());
        }
        sb.append("]");
      }
      sb.append("]}");
      return sb.toString();
    }
  }

  public static class MyParameterizedType<T> {
    private final T value;
    public MyParameterizedType(T value) {
      this.value = value;
    }
    public T getValue() {
      return value;
    }

    @SuppressWarnings("unchecked")
    public String getExpectedJson() {
      Class<T> clazz = (Class<T>) value.getClass();
      boolean addQuotes = !clazz.isArray() && !Primitives.unwrap(clazz).isPrimitive();
      StringBuilder sb = new StringBuilder("{\"");
      sb.append(value.getClass().getSimpleName()).append("\":");
      if (addQuotes) {
        sb.append("\"");
      }
      sb.append(value.toString());
      if (addQuotes) {
        sb.append("\"");
      }
      sb.append("}");
      return sb.toString();
    }
  }

  public static enum MyEnum {
    VALUE1, VALUE2;

    public String getExpectedJson() {
      return "\"" + toString() + "\"";
    }
  }

  public static class ClassWithEnumFields {
    private final MyEnum value1 = MyEnum.VALUE1;
    private final MyEnum value2 = MyEnum.VALUE2;
    public String getExpectedJson() {
      return "{\"value1\":\"" + value1 + "\",\"value2\":\"" + value2 + "\"}";
    }
  }

  public static class MyEnumCreator implements InstanceCreator<MyEnum> {
    public MyEnum createInstance(Type type) {
      return MyEnum.VALUE1;
    }
  }

  public static class ClassWithPrivateNoArgsConstructor {
    public int a;
    private ClassWithPrivateNoArgsConstructor() {
      a = 10;
    }
  }

  public static class ClassOverridingEquals {
    public ClassOverridingEquals ref;

    public String getExpectedJson() {
      if (ref == null) {
        return "{}";
      }
      return "{\"ref\":" + ref.getExpectedJson() + "}";
    }
    @Override
    public boolean equals(Object obj) {
      return true;
    }

    @Override
    public int hashCode() {
      return 1;
    }
  }

  public static class ClassWithExposedFields {
    @Expose int a = 1;
    int b = 2;

    public String getExpectedJson() {
      return '{' + "\"a\":" + a + '}';
    }

    public String getExpectedJsonWithoutAnnotations() {
      return '{' + "\"a\":" + a + ",\"b\":" + b + '}';
    }
  }

  public static class ClassWithArray {
    public final Object[] array;
    public ClassWithArray() {
      array = null;
    }

    public ClassWithArray(Object[] array) {
      this.array = array;
    }
  }

  public static class ClassWithObjects {
    public final BagOfPrimitives bag;
    public ClassWithObjects() {
      this(new BagOfPrimitives());
    }
    public ClassWithObjects(BagOfPrimitives bag) {
      this.bag = bag;
    }
  }

  public static class ClassWithSerializedNameFields {
    @SerializedName("fooBar") public final int f;

    public ClassWithSerializedNameFields() {
      this(1);
    }
    public ClassWithSerializedNameFields(int f) {
      this.f = f;
    }

    public String getExpectedJson() {
      return '{' + "\"fooBar\":" + f + '}';
    }
  }
}