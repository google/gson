/*
 * Copyright (C) 2016 Gson Authors
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

package com.google.gson.typeadapters;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.junit.Test;

public class PostConstructAdapterFactoryTest {
  @Test
  public void test() throws Exception {
    Gson gson =
        new GsonBuilder().registerTypeAdapterFactory(new PostConstructAdapterFactory()).create();
    Sandwich unused =
        gson.fromJson("{\"bread\": \"white\", \"cheese\": \"cheddar\"}", Sandwich.class);

    var e =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                gson.fromJson(
                    "{\"bread\": \"cheesey bread\", \"cheese\": \"swiss\"}", Sandwich.class));
    assertThat(e).hasMessageThat().isEqualTo("too cheesey");
  }

  @Test
  public void testList() {
    MultipleSandwiches sandwiches =
        new MultipleSandwiches(
            Arrays.asList(new Sandwich("white", "cheddar"), new Sandwich("whole wheat", "swiss")));

    Gson gson =
        new GsonBuilder().registerTypeAdapterFactory(new PostConstructAdapterFactory()).create();

    // Throws NullPointerException without the fix in https://github.com/google/gson/pull/1103
    String json = gson.toJson(sandwiches);
    assertThat(json)
        .isEqualTo(
            "{\"sandwiches\":[{\"bread\":\"white\",\"cheese\":\"cheddar\"},"
                + "{\"bread\":\"whole wheat\",\"cheese\":\"swiss\"}]}");

    MultipleSandwiches sandwichesFromJson = gson.fromJson(json, MultipleSandwiches.class);
    assertThat(sandwichesFromJson).isEqualTo(sandwiches);
  }

  @SuppressWarnings({"overrides", "EqualsHashCode"}) // for missing hashCode() override
  static class Sandwich {
    public String bread;
    public String cheese;

    public Sandwich(String bread, String cheese) {
      this.bread = bread;
      this.cheese = cheese;
    }

    @PostConstruct
    private void validate() {
      if (bread.equals("cheesey bread") && cheese != null) {
        throw new IllegalArgumentException("too cheesey");
      }
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Sandwich)) {
        return false;
      }
      Sandwich other = (Sandwich) o;

      return Objects.equals(this.bread, other.bread) && Objects.equals(this.cheese, other.cheese);
    }
  }

  @SuppressWarnings({"overrides", "EqualsHashCode"}) // for missing hashCode() override
  static class MultipleSandwiches {
    public List<Sandwich> sandwiches;

    public MultipleSandwiches(List<Sandwich> sandwiches) {
      this.sandwiches = sandwiches;
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof MultipleSandwiches)) {
        return false;
      }
      MultipleSandwiches other = (MultipleSandwiches) o;

      return Objects.equals(this.sandwiches, other.sandwiches);
    }
  }
}
