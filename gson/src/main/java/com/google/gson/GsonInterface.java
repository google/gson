package com.google.gson;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

/**
 * Interface for Gson.  Using this interface in client code makes it possible to mock, and thereby
 * unit test more cleanly.
 *
 * Created by art on 4/10/16.
 */
public interface GsonInterface {

  @SuppressWarnings("unchecked")
  <T> TypeAdapter<T> getAdapter(TypeToken<T> type);

  <T> TypeAdapter<T> getDelegateAdapter(TypeAdapterFactory skipPast, TypeToken<T> type);

  <T> TypeAdapter<T> getAdapter(Class<T> type);

  JsonElement toJsonTree(Object src);

  JsonElement toJsonTree(Object src, Type typeOfSrc);

  String toJson(Object src);

  String toJson(Object src, Type typeOfSrc);

  void toJson(Object src, Appendable writer) throws JsonIOException;

  void toJson(Object src, Type typeOfSrc, Appendable writer) throws JsonIOException;

  @SuppressWarnings("unchecked")
  void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws JsonIOException;

  String toJson(JsonElement jsonElement);

  void toJson(JsonElement jsonElement, Appendable writer) throws JsonIOException;

  JsonWriter newJsonWriter(Writer writer) throws IOException;

  JsonReader newJsonReader(Reader reader);

  void toJson(JsonElement jsonElement, JsonWriter writer) throws JsonIOException;

  <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException;

  @SuppressWarnings("unchecked")
  <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException;

  <T> T fromJson(Reader json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException;

  @SuppressWarnings("unchecked")
  <T> T fromJson(Reader json, Type typeOfT) throws JsonIOException, JsonSyntaxException;

  @SuppressWarnings("unchecked")
  <T> T fromJson(JsonReader reader, Type typeOfT) throws JsonIOException, JsonSyntaxException;

  <T> T fromJson(JsonElement json, Class<T> classOfT) throws JsonSyntaxException;

  @SuppressWarnings("unchecked")
  <T> T fromJson(JsonElement json, Type typeOfT) throws JsonSyntaxException;

  @Override
  String toString();
}
