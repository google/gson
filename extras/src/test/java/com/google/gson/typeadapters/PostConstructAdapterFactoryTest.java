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

import javax.annotation.PostConstruct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class PostConstructAdapterFactoryTest extends TestCase {
    public void test() throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new PostConstructAdapterFactory())
                .create();
        gson.fromJson("{\"bread\": \"white\", \"cheese\": \"cheddar\"}", Sandwich.class);
        try {
            gson.fromJson("{\"bread\": \"cheesey bread\", \"cheese\": \"swiss\"}", Sandwich.class);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("too cheesey", expected.getMessage());
        }
    }

    public void testList() {
        MultipleSandwiches sandwiches = new MultipleSandwiches(Arrays.asList(
            new Sandwich("white", "cheddar"),
            new Sandwich("whole wheat", "swiss")));

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new PostConstructAdapterFactory()).create();

        // Throws NullPointerException without the fix in https://github.com/google/gson/pull/1103
        String json = gson.toJson(sandwiches);
        assertEquals("{\"sandwiches\":[{\"bread\":\"white\",\"cheese\":\"cheddar\"},{\"bread\":\"whole wheat\",\"cheese\":\"swiss\"}]}", json);

        MultipleSandwiches sandwichesFromJson = gson.fromJson(json, MultipleSandwiches.class);
        assertEquals(sandwiches, sandwichesFromJson);
    }

    static class Sandwich {
        public String bread;
        public String cheese;

        public Sandwich(String bread, String cheese) {
            this.bread = bread;
            this.cheese = cheese;
        }

        @PostConstruct private void validate() {
            if (bread.equals("cheesey bread") && cheese != null) {
                throw new IllegalArgumentException("too cheesey");
            }
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Sandwich)) {
                return false;
            }
            final Sandwich other = (Sandwich) o;
            if (this.bread == null ? other.bread != null : !this.bread.equals(other.bread)) {
                return false;
            }
            if (this.cheese == null ? other.cheese != null : !this.cheese.equals(other.cheese)) {
                return false;
            }
            return true;
        }
    }

    static class MultipleSandwiches {
        public List<Sandwich> sandwiches;

        public MultipleSandwiches(List<Sandwich> sandwiches) {
            this.sandwiches = sandwiches;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof MultipleSandwiches)) {
                return false;
            }
            final MultipleSandwiches other = (MultipleSandwiches) o;
            if (this.sandwiches == null ? other.sandwiches != null : !this.sandwiches.equals(other.sandwiches)) {
                return false;
            }
            return true;
        }
    }
}
