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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class acts as a namespace for holding all the "default"
 * {@link InstanceCreator}s that are used with the Gson.
 *
 * This class contains all the Primitive type instance creator because
 * primitives should not be added directly as a GUICE provider since
 * injecting a primitive value that's scoped as a Singleton (or not
 * scoped at all) can cause major confusion.
 *
 * @author Joel Leitch
 */
final class DefaultInstanceCreators {

  static Map<Type, InstanceCreator<?>> getDefaultInstanceCreators() {
    Map<Type, InstanceCreator<?>> map = new LinkedHashMap<Type, InstanceCreator<?>>();

    // Add primitive instance creators
    map.put(Boolean.class, new BooleanCreator());
    map.put(boolean.class, new BooleanCreator());
    map.put(Byte.class, new ByteCreator());
    map.put(byte.class, new ByteCreator());
    map.put(Character.class, new CharacterCreator());
    map.put(char.class, new CharacterCreator());
    map.put(Double.class, new DoubleCreator());
    map.put(double.class, new DoubleCreator());
    map.put(Float.class, new FloatCreator());
    map.put(float.class, new FloatCreator());
    map.put(Integer.class, new IntegerCreator());
    map.put(int.class, new IntegerCreator());
    map.put(Long.class, new LongCreator());
    map.put(long.class, new LongCreator());
    map.put(Short.class, new ShortCreator());
    map.put(short.class, new ShortCreator());

    // Add Collection instance creators
    InstanceCreator<LinkedList<?>> linkedListCreator = new LinkedListCreator();
    map.put(Collection.class, linkedListCreator);
    map.put(List.class, linkedListCreator);
    map.put(Queue.class, linkedListCreator);

    // Add Set instance creators
    InstanceCreator<TreeSet<?>> treeSetCreator = new TreeSetCreator();
    map.put(Set.class, treeSetCreator);
    map.put(SortedSet.class, treeSetCreator);

    // Add instance creators for other common objects
    map.put(Enum.class, new EnumCreator());
    map.put(Map.class, new MapCreator());
    map.put(URL.class, new UrlCreator());
    map.put(Locale.class, new LocaleCreator());

    return map;
  }

  private static class EnumCreator implements InstanceCreator<Enum<?>> {
    @SuppressWarnings("unchecked")
    public Enum<?> createInstance(Type type) {
      Class<Enum<?>> enumClass = (Class<Enum<?>>) type;
      try {
        Method valuesMethod = enumClass.getMethod("values");
        Enum<?>[] enums = (Enum<?>[]) valuesMethod.invoke(null);
        return enums[0];
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class LongCreator implements InstanceCreator<Long> {
    public Long createInstance(Type type) {
      return new Long(0L);
    }
  }

  private static class IntegerCreator implements InstanceCreator<Integer> {
    public Integer createInstance(Type type) {
      return new Integer(0);
    }
  }

  private static class ShortCreator implements InstanceCreator<Short> {
    public Short createInstance(Type type) {
      return new Short((short) 0);
    }
  }

  private static class ByteCreator implements InstanceCreator<Byte> {
    public Byte createInstance(Type type) {
      return new Byte((byte) 0);
    }
  }

  private static class FloatCreator implements InstanceCreator<Float> {
    public Float createInstance(Type type) {
      return new Float(0F);
    }
  }

  private static class DoubleCreator implements InstanceCreator<Double> {
    public Double createInstance(Type type) {
      return new Double(0D);
    }
  }

  private static class CharacterCreator implements InstanceCreator<Character> {
    public Character createInstance(Type type) {
      return new Character((char) 0);
    }
  }

  private static class BooleanCreator implements InstanceCreator<Boolean> {
    public Boolean createInstance(Type type) {
      return new Boolean(false);
    }
  }

  private static class LinkedListCreator implements InstanceCreator<LinkedList<?>> {
    public LinkedList<?> createInstance(Type type) {
      return new LinkedList<Object>();
    }
  }

  private static class TreeSetCreator implements InstanceCreator<TreeSet<?>> {
    public TreeSet<?> createInstance(Type type) {
      return new TreeSet<Object>();
    }
  }

  private static class UrlCreator implements InstanceCreator<URL> {
    public URL createInstance(Type type) {
      try {
        return new URL("http://google.com/");
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class LocaleCreator implements InstanceCreator<Locale> {
    public Locale createInstance(Type type) {
      return new Locale("en_US");
    }
  }

  @SuppressWarnings("unchecked")
  private static class MapCreator implements InstanceCreator<Map> {

    @SuppressWarnings("unchecked")
    public Map createInstance(Type type) {
      return new LinkedHashMap();
    }
  }
}
