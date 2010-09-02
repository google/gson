/*
 * Copyright (C) 2010 Google Inc.
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

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import junit.framework.TestCase;

public final class MixedStreamTest extends TestCase {

  private static final Car BLUE_MUSTANG = new Car("mustang", 0x0000FF);
  private static final Car BLACK_BMW = new Car("bmw", 0x000000);
  private static final Car RED_MIATA = new Car("miata", 0xFF0000);
  private static final String CARS_JSON = "[\n"
      + "  {\n"
      + "    \"name\": \"mustang\",\n"
      + "    \"color\": 255\n"
      + "  },\n"
      + "  {\n"
      + "    \"name\": \"bmw\",\n"
      + "    \"color\": 0\n"
      + "  },\n"
      + "  {\n"
      + "    \"name\": \"miata\",\n"
      + "    \"color\": 16711680\n"
      + "  }\n"
      + "]";

  public void testWriteMixedStreamed() throws IOException {
    Gson gson = new Gson();
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);

    jsonWriter.beginArray();
    jsonWriter.setIndent("  ");
    gson.toJson(BLUE_MUSTANG, Car.class, jsonWriter);
    gson.toJson(BLACK_BMW, Car.class, jsonWriter);
    gson.toJson(RED_MIATA, Car.class, jsonWriter);
    jsonWriter.endArray();

    assertEquals(CARS_JSON, stringWriter.toString());
  }

  public void testReadMixedStreamed() throws IOException {
    Gson gson = new Gson();
    StringReader stringReader = new StringReader(CARS_JSON);
    JsonReader jsonReader = new JsonReader(stringReader);

    jsonReader.beginArray();
    assertEquals(BLUE_MUSTANG, gson.fromJson(jsonReader, Car.class));
    assertEquals(BLACK_BMW, gson.fromJson(jsonReader, Car.class));
    assertEquals(RED_MIATA, gson.fromJson(jsonReader, Car.class));
    jsonReader.endArray();
  }

  static final class Car {
    String name;
    int color;

    Car(String name, int color) {
      this.name = name;
      this.color = color;
    }

    Car() {} // for Gson

    @Override public int hashCode() {
      return name.hashCode() ^ color;
    }

    @Override public boolean equals(Object o) {
      return o instanceof Car
          && ((Car) o).name.equals(name)
          && ((Car) o).color == color;
    }
  }
}
