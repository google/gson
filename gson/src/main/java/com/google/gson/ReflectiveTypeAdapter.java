package com.google.gson;

import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class ReflectiveTypeAdapter<T> extends TypeAdapter<T> {

  public TypeAdapterFactory getFactory(final Type type) {
    final ReflectiveTypeAdapter<T> thisClass = this;
    TypeAdapterFactory factory = new TypeAdapterFactory() {
      @Override
      public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(typeToken,
            thisClass);
        List<Type> typeList = new ArrayList<Type>();
        typeList.add(type);
        ReflectiveTypeAdapterFactory reflectiveTypeAdapterFactory =
            new ReflectiveTypeAdapterFactory(constructorConstructor, gson.fieldNamingStrategy,
                gson.excluder, gson.jsonAdapterFactory, typeList);
        return reflectiveTypeAdapterFactory.create(gson, typeToken);
      }
    };
    return factory;
  }

}
