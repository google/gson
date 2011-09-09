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

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Adapts a BigInteger type to and from its JSON representation.
 *
 * @author Joel Leitch
 */
public class BigIntegerTypeAdapter extends TypeAdapter<BigInteger> {

  @Override
  public BigInteger read(JsonReader reader) throws IOException {
    try {
      return new BigInteger(reader.nextString());
    } catch (NumberFormatException e) {
      throw new JsonSyntaxException(e);
    }
  }

  @Override
  public void write(JsonWriter writer, BigInteger value) throws IOException {
    writer.value(value);
  }
}
