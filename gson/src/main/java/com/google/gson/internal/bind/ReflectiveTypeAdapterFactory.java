/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.internal.bind;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.MethodAndFieldNamingPolicy;
import com.google.gson.MethodAndFieldNamingStrategy;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.GsonGetter;
import com.google.gson.annotations.GsonSetter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter;

/**
 * Type adapter that reflects over the fields and methods of a class.
 */
public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;
  private final FieldNamingStrategy fieldNamingPolicy;
  private final MethodAndFieldNamingStrategy methodAndFieldNamingPolicy;
  private final Excluder excluder;

  public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor,
      FieldNamingStrategy namingPolicy, Excluder excluder) {
    this.constructorConstructor = constructorConstructor;
    this.methodAndFieldNamingPolicy = namingPolicy instanceof MethodAndFieldNamingPolicy
    		? (MethodAndFieldNamingPolicy)namingPolicy
    		: null;
    this.fieldNamingPolicy = namingPolicy;
    this.excluder = excluder;
  }

  public boolean excludeField(Field f, boolean serialize) {
    return excludeField(f, serialize, excluder);
  }

  static boolean excludeField(Field f, boolean serialize, Excluder excluder) {
    return !excluder.excludeClass(f.getType(), serialize) && !excluder.excludeField(f, serialize);
  }

  private String getFieldName(Field f) {
    return getFieldName(fieldNamingPolicy, f);
  }
  
  private String getMethodName(Method m) {
	  if (methodAndFieldNamingPolicy == null) {
		  throw new RuntimeException("You are trying to translate a method into a json property name with a FieldNamingStrategy.  You'll need to implement a MethodAndFieldNamingStrategy and provide that to your Gson instance.");
	  }
	  return getMethodName(methodAndFieldNamingPolicy, m);
  }

  static String getFieldName(FieldNamingStrategy fieldNamingPolicy, Field f) {
    SerializedName serializedName = f.getAnnotation(SerializedName.class);
    return serializedName == null ? fieldNamingPolicy.translateName(f) : serializedName.value();
  }
  
  private static String getMethodName(MethodAndFieldNamingStrategy methodAndFieldNamingPolicy, Method m) {
    SerializedName serializedName = m.getAnnotation(SerializedName.class);
    return serializedName == null ? methodAndFieldNamingPolicy.translateName(m) : serializedName.value();
  }

  public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
    Class<? super T> raw = type.getRawType();

    if (!Object.class.isAssignableFrom(raw)) {
      return null; // it's a primitive!
    }

    ObjectConstructor<T> constructor = constructorConstructor.get(type);
    return new Adapter<T>(constructor, new BoundProperties(getBoundFields(gson, type, raw), getBoundMethods(gson, type, raw)));
  }

  private ReflectiveTypeAdapterFactory.BoundField createBoundField(
      final Gson context, final Field field, final String name,
      final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
    final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
    // special casing primitives here saves ~5% on Android...
    return new ReflectiveTypeAdapterFactory.BoundField(name, serialize, deserialize) {
      final TypeAdapter<?> typeAdapter = getFieldAdapter(context, field, fieldType);
      @SuppressWarnings({"unchecked", "rawtypes"}) // the type adapter and field type always agree
      @Override void write(JsonWriter writer, Object value)
          throws IOException, IllegalAccessException {
        Object fieldValue = field.get(value);
        TypeAdapter t =
          new TypeAdapterRuntimeTypeWrapper(context, this.typeAdapter, fieldType.getType());
        t.write(writer, fieldValue);
      }
      @Override void read(JsonReader reader, Object value)
          throws IOException, IllegalAccessException {
        Object fieldValue = typeAdapter.read(reader);
        if (fieldValue != null || !isPrimitive) {
          field.set(value, fieldValue);
        }
      }
      public boolean writeField(Object value) throws IOException, IllegalAccessException {
        if (!serialized) return false;
        Object fieldValue = field.get(value);
        return fieldValue != value; // avoid recursion for example for Throwable.cause
      }
    };
  }
  
  private ReflectiveTypeAdapterFactory.BoundMethod createBoundMethod(
	    final Gson context, final Method method, final String name,
	    final TypeToken<?> propertyType, boolean serialize, boolean deserialize) {
	    // special casing primitives here saves ~5% on Android...
	    return new ReflectiveTypeAdapterFactory.BoundMethod(name, serialize, deserialize) {
	      final TypeAdapter<?> typeAdapter = getMethodAdapter(context, method, propertyType);
	      @SuppressWarnings({"unchecked", "rawtypes"}) // the type adapter and field type always agree
	      @Override void write(JsonWriter writer, Object value)
	          throws IOException, IllegalAccessException {
	    	Object returnValue = this.invokeGetter(value, method);
	        TypeAdapter t =
	          new TypeAdapterRuntimeTypeWrapper(context, this.typeAdapter, propertyType.getType());
	        t.write(writer, returnValue);
	      }
	      @Override void read(JsonReader reader, Object value)
	          throws IOException, IllegalAccessException {
	        Object inputValue = typeAdapter.read(reader);
	        this.invokeSetter(value, method, inputValue);
	        try {
				method.invoke(value, inputValue);
			} catch (IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
	      }
	      
	      private Object invokeGetter(Object self, Method method) {
			Object returnValue;
				try {
					returnValue = method.invoke(self);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
            return returnValue;
	      }
	      
	      private void invokeSetter(Object self, Method method, Object value) {
	    	try {
				method.invoke(self, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
	      }
	    };
	  }

  private TypeAdapter<?> getFieldAdapter(Gson gson, Field field, TypeToken<?> fieldType) {
    JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
    if (annotation != null) {
      TypeAdapter<?> adapter = getTypeAdapter(constructorConstructor, gson, fieldType, annotation);
      if (adapter != null) return adapter;
    }
    return gson.getAdapter(fieldType);
  }

  private TypeAdapter<?> getMethodAdapter(Gson gson, Method method, TypeToken<?> propertyType) {
    JsonAdapter annotation = method.getAnnotation(JsonAdapter.class);
    if (annotation != null) {
      TypeAdapter<?> adapter = getTypeAdapter(constructorConstructor, gson, propertyType, annotation);
      if (adapter != null) return adapter;
    }
    return gson.getAdapter(propertyType);
  }
  
  private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw) {
    Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
    if (raw.isInterface()) {
      return result;
    }

    Type declaredType = type.getType();
    while (raw != Object.class) {
      Field[] fields = raw.getDeclaredFields();
      for (Field field : fields) {
        boolean serialize = excludeField(field, true);
        boolean deserialize = excludeField(field, false);
        if (!serialize && !deserialize) {
          continue;
        }
        field.setAccessible(true);
        Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
        BoundField boundField = createBoundField(context, field, getFieldName(field),
            TypeToken.get(fieldType), serialize, deserialize);
        BoundField previous = result.put(boundField.name, boundField);
        if (previous != null) {
          throw new IllegalArgumentException(declaredType
              + " declares multiple JSON fields named " + previous.name);
        }
      }
      type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
      raw = type.getRawType();
    }
    return result;
  }
  
  private BoundMethods getBoundMethods(Gson context, TypeToken<?> type, Class<?> raw) {
    Map<String, BoundMethod> getters = new LinkedHashMap<String, BoundMethod>();
    Map<String, BoundMethod> setters = new LinkedHashMap<String, BoundMethod>();
    if (raw.isInterface()) {
      return new BoundMethods(getters, setters);
    }

    Type declaredType = type.getType();
    while (raw != Object.class) {
      Method[] methods = raw.getDeclaredMethods();
      for (Method method : methods) {
    	boolean serialize = false;
    	boolean deserialize = false;
    	boolean hasExposeGet = method.getAnnotation(GsonGetter.class) != null;
    	boolean hasExposeSet = method.getAnnotation(GsonSetter.class) != null;
    	if (hasExposeGet && hasExposeSet) {
    		throw new RuntimeException("You cannot specify a method as both a GsonGetter and GsonSetter");
    	} else if (hasExposeGet) {
    		serialize = true;
    	} else if (hasExposeSet) {
    		deserialize = true;
    	}
        if (!serialize && !deserialize) {
          continue;
        }
        //TODO consider throwing for non public methods, static methods, etc. depending on GsonGetter and GsonSetter
        //
        method.setAccessible(true);
        Type[] parameterTypes = method.getGenericParameterTypes();

		if (serialize) {
        	if (parameterTypes.length != 0) {
        		throw new RuntimeException("Gson getters must have zero parameters");
        	}
        	Type propertyType = $Gson$Types.resolve(type.getType(), raw, method.getGenericReturnType());
        	if (propertyType.equals(Void.TYPE)) {
        		throw new RuntimeException("Gson getters must not return void");
        	}
        	BoundMethod boundMethod = createBoundMethod(context, method, getMethodName(method),
        		TypeToken.get(propertyType), serialize, deserialize);
        	BoundMethod previous = getters.put(boundMethod.name, boundMethod);
        	if (previous != null) {
        		throw new IllegalArgumentException(declaredType
        			+ " declares multiple gson getters named " + previous.name);
        	}        
        } else {
        	if (parameterTypes.length != 1) {
        		throw new RuntimeException("Gson setters must have exactly one parameter");
        	}
        	Type propertyType = $Gson$Types.resolve(type.getType(), raw, parameterTypes[0]);
        	BoundMethod boundMethod = createBoundMethod(context, method, getMethodName(method),
        		TypeToken.get(propertyType), serialize, deserialize);
        	BoundMethod previous = setters.put(boundMethod.name, boundMethod);
        	if (previous != null) {
        		throw new IllegalArgumentException(declaredType
        			+ " declares multiple gson setters named " + previous.name);
        	}
        }
      }
      type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
      raw = type.getRawType();
    }
    return new BoundMethods(getters, setters);
  }

  static abstract class BoundField {
    final String name;
    final boolean serialized;
    final boolean deserialized;

    protected BoundField(String name, boolean serialized, boolean deserialized) {
      this.name = name;
      this.serialized = serialized;
      this.deserialized = deserialized;
    }
    abstract boolean writeField(Object value) throws IOException, IllegalAccessException;
    abstract void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException;
    abstract void read(JsonReader reader, Object value) throws IOException, IllegalAccessException;
  }
  
  static abstract class BoundMethod {
    final String name;
    final boolean serialized;
    final boolean deserialized;

    protected BoundMethod(String name, boolean serialized, boolean deserialized) {
      this.name = name;
      this.serialized = serialized;
      this.deserialized = deserialized;
    }
    abstract void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException;
    abstract void read(JsonReader reader, Object value) throws IOException, IllegalAccessException;
  }
  
  private static class BoundMethods {
	  private final Map<String, BoundMethod> getters;
	  private final Map<String, BoundMethod> setters;
	  
	  private BoundMethods(Map<String, BoundMethod> getters, Map<String, BoundMethod> setters) {
		  this.getters = getters;
		  this.setters = setters;
	  }
	  
	  private Collection<BoundMethod> getGetters() {
		  return this.getters.values();
	  }
	  
	  private BoundMethod getSetter(String name) {
		  return this.setters.get(name);
	  }
  }
  
  private static class BoundProperties {
	  private final BoundMethods boundMethods;
	  private final Map<String, BoundField> boundFields;
	  
	  private BoundProperties(Map<String, BoundField> boundFields, BoundMethods boundMethods) {
		  this.boundFields = boundFields;
		  this.boundMethods = boundMethods;
		  for (String getter : this.boundMethods.getters.keySet()) {
			  this.boundFields.remove(getter);
		  }
	  }
	  
	  private BoundField getField(String name) {
		  return this.boundFields.get(name);
	  }
	  
	  private BoundMethod getSetter(String name) {
		  return this.boundMethods.getSetter(name);
	  }
	  
	  private Collection<BoundMethod> getGetters() {
		  return this.boundMethods.getGetters();
	  }
	  
	  private Collection<BoundField> getFields() {
		  return this.boundFields.values();
	  }
  }

  public static final class Adapter<T> extends TypeAdapter<T> {
    private final ObjectConstructor<T> constructor;
    private final BoundProperties boundProperties;

    private Adapter(ObjectConstructor<T> constructor, BoundProperties boundProperties) {
      this.constructor = constructor;
      this.boundProperties = boundProperties;
    }

    @Override public T read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      T instance = constructor.construct();

      try {
        in.beginObject();
        while (in.hasNext()) {
          String name = in.nextName();
          BoundField field = boundProperties.getField(name);
          BoundMethod method = boundProperties.getSetter(name);
          if (field != null && field.deserialized) {
        	  field.read(in,  instance);
          } else if (method != null && method.deserialized) {
        	  method.read(in, instance);
          } else {
            in.skipValue();
          }
        }
      } catch (IllegalStateException e) {
        throw new JsonSyntaxException(e);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
      in.endObject();
      return instance;
    }

    @Override public void write(JsonWriter out, T value) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }

      out.beginObject();
      try {
        for (BoundField boundField : boundProperties.getFields()) {
          if (boundField.writeField(value)) {
            out.name(boundField.name);
            boundField.write(out, value);
          }
        }
        for (BoundMethod boundMethod : boundProperties.getGetters()) {
          out.name(boundMethod.name);
          boundMethod.write(out, value);
        }
      } catch (IllegalAccessException e) {
        throw new AssertionError();
      }
      out.endObject();
    }
  }
}
