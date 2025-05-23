/*
 * Copyright (C) 2009 Google Inc.
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
package com.google.gson.functional;

import static com.google.common.truth.Truth.assertThat;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.common.TestTypes.BagOfPrimitives;
import com.google.gson.common.TestTypes.Base;
import com.google.gson.common.TestTypes.ClassWithBaseArrayField;
import com.google.gson.common.TestTypes.ClassWithBaseCollectionField;
import com.google.gson.common.TestTypes.ClassWithBaseField;
import com.google.gson.common.TestTypes.Nested;
import com.google.gson.common.TestTypes.Sub;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for Json serialization and deserialization of classes with inheritance
 * hierarchies.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class InheritanceTest {
  private Gson gson;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
  }

  @Test
  public void testSubClassSerialization() {
    SubTypeOfNested target =
        new SubTypeOfNested(
            new BagOfPrimitives(10, 20, false, "stringValue"),
            new BagOfPrimitives(30, 40, true, "stringValue"));
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testSubClassDeserialization() {
    String json =
        "{\"value\":5,\"primitive1\":{\"longValue\":10,\"intValue\":20,"
            + "\"booleanValue\":false,\"stringValue\":\"stringValue\"},\"primitive2\":"
            + "{\"longValue\":30,\"intValue\":40,\"booleanValue\":true,"
            + "\"stringValue\":\"stringValue\"}}";
    SubTypeOfNested target = gson.fromJson(json, SubTypeOfNested.class);
    assertThat(target.getExpectedJson()).isEqualTo(json);
  }

  @Test
  public void testClassWithBaseFieldSerialization() {
    ClassWithBaseField sub = new ClassWithBaseField(new Sub());
    JsonObject json = (JsonObject) gson.toJsonTree(sub);
    JsonElement base = json.getAsJsonObject().get(ClassWithBaseField.FIELD_KEY);
    assertThat(base.getAsJsonObject().get(Sub.SUB_FIELD_KEY).getAsString()).isEqualTo(Sub.SUB_NAME);
  }

  @Test
  public void testClassWithBaseArrayFieldSerialization() {
    Base[] baseClasses = new Base[] {new Sub(), new Sub()};
    ClassWithBaseArrayField sub = new ClassWithBaseArrayField(baseClasses);
    JsonObject json = gson.toJsonTree(sub).getAsJsonObject();
    JsonArray bases = json.get(ClassWithBaseArrayField.FIELD_KEY).getAsJsonArray();
    for (JsonElement element : bases) {
      assertThat(element.getAsJsonObject().get(Sub.SUB_FIELD_KEY).getAsString())
          .isEqualTo(Sub.SUB_NAME);
    }
  }

  @Test
  public void testClassWithBaseCollectionFieldSerialization() {
    Collection<Base> baseClasses = new ArrayList<>();
    baseClasses.add(new Sub());
    baseClasses.add(new Sub());
    ClassWithBaseCollectionField sub = new ClassWithBaseCollectionField(baseClasses);
    JsonObject json = gson.toJsonTree(sub).getAsJsonObject();
    JsonArray bases = json.get(ClassWithBaseArrayField.FIELD_KEY).getAsJsonArray();
    for (JsonElement element : bases) {
      assertThat(element.getAsJsonObject().get(Sub.SUB_FIELD_KEY).getAsString())
          .isEqualTo(Sub.SUB_NAME);
    }
  }

  @Test
  public void testBaseSerializedAsSub() {
    Base base = new Sub();
    JsonObject json = gson.toJsonTree(base).getAsJsonObject();
    assertThat(json.get(Sub.SUB_FIELD_KEY).getAsString()).isEqualTo(Sub.SUB_NAME);
  }

  @Test
  public void testBaseSerializedAsSubForToJsonMethod() {
    Base base = new Sub();
    String json = gson.toJson(base);
    assertThat(json).contains(Sub.SUB_NAME);
  }

  @Test
  public void testBaseSerializedAsBaseWhenSpecifiedWithExplicitType() {
    Base base = new Sub();
    JsonObject json = gson.toJsonTree(base, Base.class).getAsJsonObject();
    assertThat(json.get(Base.BASE_FIELD_KEY).getAsString()).isEqualTo(Base.BASE_NAME);
    assertThat(json.get(Sub.SUB_FIELD_KEY)).isNull();
  }

  @Test
  public void testBaseSerializedAsBaseWhenSpecifiedWithExplicitTypeForToJsonMethod() {
    Base base = new Sub();
    String json = gson.toJson(base, Base.class);
    assertThat(json).contains(Base.BASE_NAME);
    assertThat(json).doesNotContain(Sub.SUB_FIELD_KEY);
  }

  @Test
  public void testBaseSerializedAsSubWhenSpecifiedWithExplicitType() {
    Base base = new Sub();
    JsonObject json = gson.toJsonTree(base, Sub.class).getAsJsonObject();
    assertThat(json.get(Sub.SUB_FIELD_KEY).getAsString()).isEqualTo(Sub.SUB_NAME);
  }

  @Test
  public void testBaseSerializedAsSubWhenSpecifiedWithExplicitTypeForToJsonMethod() {
    Base base = new Sub();
    String json = gson.toJson(base, Sub.class);
    assertThat(json).contains(Sub.SUB_NAME);
  }

  private static class SubTypeOfNested extends Nested {
    private final long value = 5;

    public SubTypeOfNested(BagOfPrimitives primitive1, BagOfPrimitives primitive2) {
      super(primitive1, primitive2);
    }

    @Override
    public void appendFields(StringBuilder sb) {
      sb.append("\"value\":").append(value).append(",");
      super.appendFields(sb);
    }
  }

  @Test
  @SuppressWarnings("JdkObsolete")
  public void testSubInterfacesOfCollectionSerialization() {
    List<Integer> list = new LinkedList<>();
    list.add(0);
    list.add(1);
    list.add(2);
    list.add(3);
    Queue<Long> queue = new LinkedList<>();
    queue.add(0L);
    queue.add(1L);
    queue.add(2L);
    queue.add(3L);
    Set<Float> set = new TreeSet<>();
    set.add(0.1F);
    set.add(0.2F);
    set.add(0.3F);
    set.add(0.4F);
    SortedSet<Character> sortedSet = new TreeSet<>();
    sortedSet.add('a');
    sortedSet.add('b');
    sortedSet.add('c');
    sortedSet.add('d');
    ClassWithSubInterfacesOfCollection target =
        new ClassWithSubInterfacesOfCollection(list, queue, set, sortedSet);
    assertThat(gson.toJson(target)).isEqualTo(target.getExpectedJson());
  }

  @Test
  public void testSubInterfacesOfCollectionDeserialization() {
    String json =
        "{\"list\":[0,1,2,3],\"queue\":[0,1,2,3],\"set\":[0.1,0.2,0.3,0.4],"
            + "\"sortedSet\":[\"a\",\"b\",\"c\",\"d\"]"
            + "}";
    ClassWithSubInterfacesOfCollection target =
        gson.fromJson(json, ClassWithSubInterfacesOfCollection.class);
    assertThat(target.listContains(0, 1, 2, 3)).isTrue();
    assertThat(target.queueContains(0, 1, 2, 3)).isTrue();
    assertThat(target.setContains(0.1F, 0.2F, 0.3F, 0.4F)).isTrue();
    assertThat(target.sortedSetContains('a', 'b', 'c', 'd')).isTrue();
  }

  private static class ClassWithSubInterfacesOfCollection {
    private List<Integer> list;
    private Queue<Long> queue;
    private Set<Float> set;
    private SortedSet<Character> sortedSet;

    public ClassWithSubInterfacesOfCollection(
        List<Integer> list, Queue<Long> queue, Set<Float> set, SortedSet<Character> sortedSet) {
      this.list = list;
      this.queue = queue;
      this.set = set;
      this.sortedSet = sortedSet;
    }

    boolean listContains(int... values) {
      for (int value : values) {
        if (!list.contains(value)) {
          return false;
        }
      }
      return true;
    }

    boolean queueContains(long... values) {
      for (long value : values) {
        if (!queue.contains(value)) {
          return false;
        }
      }
      return true;
    }

    boolean setContains(float... values) {
      for (float value : values) {
        if (!set.contains(value)) {
          return false;
        }
      }
      return true;
    }

    boolean sortedSetContains(char... values) {
      for (char value : values) {
        if (!sortedSet.contains(value)) {
          return false;
        }
      }
      return true;
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

    @CanIgnoreReturnValue
    private static StringBuilder append(StringBuilder sb, Collection<?> c) {
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
}
