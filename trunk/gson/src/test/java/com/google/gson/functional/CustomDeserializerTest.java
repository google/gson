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

package com.google.gson.functional;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import junit.framework.TestCase;

/**
 * Functional Test exercising custom deserialization only.  When test applies to both 
 * serialization and deserialization then add it to CustomTypeAdapterTest.
 *
 * @author Joel Leitch
 */
public class CustomDeserializerTest extends TestCase {
  private static final String DEFAULT_VALUE = "test123";
  private static final String SUFFIX = "blah";
  
  private Gson gson;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new GsonBuilder().registerTypeAdapter(DataHolder.class, new DataHolderDeserializer()).create();
  }
  
  public void testDefaultConstructorNotCalledOnObject() throws Exception {
    DataHolder data = new DataHolder(DEFAULT_VALUE);
    String json = gson.toJson(data);
    
    DataHolder actual = gson.fromJson(json, DataHolder.class);
    assertEquals(DEFAULT_VALUE + SUFFIX, actual.getData());
  }
  
  public void testDefaultConstructorNotCalledOnField() throws Exception {
    DataHolderWrapper dataWrapper = new DataHolderWrapper(new DataHolder(DEFAULT_VALUE));
    String json = gson.toJson(dataWrapper);
    
    DataHolderWrapper actual = gson.fromJson(json, DataHolderWrapper.class);
    assertEquals(DEFAULT_VALUE + SUFFIX, actual.getWrappedData().getData());
  }

  private static class DataHolder {
    private final String data;

    @SuppressWarnings("unused")
    public DataHolder() {
      throw new IllegalStateException();
    }
    
    public DataHolder(String data) {
      this.data = data;
    }
    
    public String getData() {
      return data;
    }
  }
  
  private static class DataHolderWrapper {
    private final DataHolder wrappedData;
    
    @SuppressWarnings("unused")
    public DataHolderWrapper() {
      this(new DataHolder(DEFAULT_VALUE));
    }
    
    public DataHolderWrapper(DataHolder data) {
      this.wrappedData = data;
    }

    public DataHolder getWrappedData() {
      return wrappedData;
    }
  }
  
  private static class DataHolderDeserializer implements JsonDeserializer<DataHolder> {
    public DataHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      JsonObject jsonObj = json.getAsJsonObject();
      String dataString = jsonObj.get("data").getAsString();
      return new DataHolder(dataString + SUFFIX);
    }
  }
}
