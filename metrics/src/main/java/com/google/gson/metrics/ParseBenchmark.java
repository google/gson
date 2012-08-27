/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.gson.metrics;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * Measure Gson and Jackson parsing and binding performance.
 *
 * <p>This benchmark requires that ParseBenchmarkData.zip is on the classpath.
 * That file contains Twitter feed data, which is representative of what
 * applications will be parsing.
 */
public final class ParseBenchmark extends SimpleBenchmark {
  @Param Document document;
  @Param Api api;

  private enum Document {
    TWEETS(new TypeToken<List<Tweet>>() {}, new TypeReference<List<Tweet>>() {}),
    READER_SHORT(new TypeToken<Feed>() {}, new TypeReference<Feed>() {}),
    READER_LONG(new TypeToken<Feed>() {}, new TypeReference<Feed>() {});

    private final Type gsonType;
    private final TypeReference<?> jacksonType;

    private Document(TypeToken<?> typeToken, TypeReference<?> typeReference) {
      this.gsonType = typeToken.getType();
      this.jacksonType = typeReference;
    }
  }

  private enum Api {
    JACKSON_STREAM {
      @Override Parser newParser() {
        return new JacksonStreamParser();
      }
    },
    JACKSON_BIND {
      @Override Parser newParser() {
        return new JacksonBindParser();
      }
    },
    GSON_STREAM {
      @Override Parser newParser() {
        return new GsonStreamParser();
      }
    },
    GSON_SKIP {
      @Override Parser newParser() {
        return new GsonSkipParser();
      }
    },
    GSON_DOM {
      @Override Parser newParser() {
        return new GsonDomParser();
      }
    },
    GSON_BIND {
      @Override Parser newParser() {
        return new GsonBindParser();
      }
    };
    abstract Parser newParser();
  }

  private char[] text;
  private Parser parser;

  @Override protected void setUp() throws Exception {
    text = resourceToString("/" + document.name() + ".json").toCharArray();
    parser = api.newParser();
  }

  public void timeParse(int reps) throws Exception {
    for (int i = 0; i < reps; i++) {
      parser.parse(text, document);
    }
  }

  private static String resourceToString(String path) throws Exception {
    InputStream in = ParseBenchmark.class.getResourceAsStream(path);
    if (in == null) {
      throw new IllegalArgumentException("No such file: " + path);
    }

    Reader reader = new InputStreamReader(in, "UTF-8");
    char[] buffer = new char[8192];
    StringWriter writer = new StringWriter();
    int count;
    while ((count = reader.read(buffer)) != -1) {
      writer.write(buffer, 0, count);
    }
    reader.close();
    return writer.toString();
  }

  public static void main(String[] args) throws Exception {
    Runner.main(ParseBenchmark.class, args);
  }

  interface Parser {
    void parse(char[] data, Document document) throws Exception;
  }

  private static class GsonStreamParser implements Parser {
    public void parse(char[] data, Document document) throws Exception {
      com.google.gson.stream.JsonReader jsonReader
          = new com.google.gson.stream.JsonReader(new CharArrayReader(data));
      readToken(jsonReader);
      jsonReader.close();
    }

    private void readToken(com.google.gson.stream.JsonReader reader) throws IOException {
      while (true) {
        switch (reader.peek()) {
        case BEGIN_ARRAY:
          reader.beginArray();
          break;
        case END_ARRAY:
          reader.endArray();
          break;
        case BEGIN_OBJECT:
          reader.beginObject();
          break;
        case END_OBJECT:
          reader.endObject();
          break;
        case NAME:
          reader.nextName();
          break;
        case BOOLEAN:
          reader.nextBoolean();
          break;
        case NULL:
          reader.nextNull();
          break;
        case NUMBER:
          reader.nextLong();
          break;
        case STRING:
          reader.nextString();
          break;
        case END_DOCUMENT:
          return;
        default:
          throw new IllegalArgumentException("Unexpected token" + reader.peek());
        }
      }
    }
  }

  private static class GsonSkipParser implements Parser {
    public void parse(char[] data, Document document) throws Exception {
      com.google.gson.stream.JsonReader jsonReader
          = new com.google.gson.stream.JsonReader(new CharArrayReader(data));
      jsonReader.skipValue();
      jsonReader.close();
    }
  }

  private static class JacksonStreamParser implements Parser {
    public void parse(char[] data, Document document) throws Exception {
      JsonFactory jsonFactory = new JsonFactory();
      org.codehaus.jackson.JsonParser jp = jsonFactory.createJsonParser(new CharArrayReader(data));
      jp.configure(org.codehaus.jackson.JsonParser.Feature.CANONICALIZE_FIELD_NAMES, false);
      int depth = 0;
      do {
        switch (jp.nextToken()) {
        case START_OBJECT:
        case START_ARRAY:
          depth++;
          break;
        case END_OBJECT:
        case END_ARRAY:
          depth--;
          break;
        case FIELD_NAME:
          jp.getCurrentName();
          break;
        case VALUE_STRING:
          jp.getText();
          break;
        case VALUE_NUMBER_INT:
        case VALUE_NUMBER_FLOAT:
          jp.getLongValue();
          break;
        }
      } while (depth > 0);
      jp.close();
    }
  }

  private static class GsonDomParser implements Parser {
    public void parse(char[] data, Document document) throws Exception {
      new JsonParser().parse(new CharArrayReader(data));
    }
  }

  private static class GsonBindParser implements Parser {
    private static Gson gson = new GsonBuilder()
        .setDateFormat("EEE MMM dd HH:mm:ss Z yyyy")
        .create();

    public void parse(char[] data, Document document) throws Exception {
      gson.fromJson(new CharArrayReader(data), document.gsonType);
    }
  }

  private static class JacksonBindParser implements Parser {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
      mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_FIELDS, true);
      mapper.setDateFormat(new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy"));
    }

    public void parse(char[] data, Document document) throws Exception {
      mapper.readValue(new CharArrayReader(data), document.jacksonType);
    }
  }

  static class Tweet {
    @JsonProperty String coordinates;
    @JsonProperty boolean favorited;
    @JsonProperty Date created_at;
    @JsonProperty boolean truncated;
    @JsonProperty Tweet retweeted_status;
    @JsonProperty String id_str;
    @JsonProperty String in_reply_to_id_str;
    @JsonProperty String contributors;
    @JsonProperty String text;
    @JsonProperty long id;
    @JsonProperty String retweet_count;
    @JsonProperty String in_reply_to_status_id_str;
    @JsonProperty Object geo;
    @JsonProperty boolean retweeted;
    @JsonProperty String in_reply_to_user_id;
    @JsonProperty String in_reply_to_screen_name;
    @JsonProperty Object place;
    @JsonProperty User user;
    @JsonProperty String source;
    @JsonProperty String in_reply_to_user_id_str;
  }

  static class User {
    @JsonProperty String name;
    @JsonProperty String profile_sidebar_border_color;
    @JsonProperty boolean profile_background_tile;
    @JsonProperty String profile_sidebar_fill_color;
    @JsonProperty Date created_at;
    @JsonProperty String location;
    @JsonProperty String profile_image_url;
    @JsonProperty boolean follow_request_sent;
    @JsonProperty String profile_link_color;
    @JsonProperty boolean is_translator;
    @JsonProperty String id_str;
    @JsonProperty int favourites_count;
    @JsonProperty boolean contributors_enabled;
    @JsonProperty String url;
    @JsonProperty boolean default_profile;
    @JsonProperty long utc_offset;
    @JsonProperty long id;
    @JsonProperty boolean profile_use_background_image;
    @JsonProperty int listed_count;
    @JsonProperty String lang;
    @JsonProperty("protected") @SerializedName("protected") boolean isProtected;
    @JsonProperty int followers_count;
    @JsonProperty String profile_text_color;
    @JsonProperty String profile_background_color;
    @JsonProperty String time_zone;
    @JsonProperty String description;
    @JsonProperty boolean notifications;
    @JsonProperty boolean geo_enabled;
    @JsonProperty boolean verified;
    @JsonProperty String profile_background_image_url;
    @JsonProperty boolean defalut_profile_image;
    @JsonProperty int friends_count;
    @JsonProperty int statuses_count;
    @JsonProperty String screen_name;
    @JsonProperty boolean following;
    @JsonProperty boolean show_all_inline_media;
  }

  static class Feed {
    @JsonProperty String id;
    @JsonProperty String title;
    @JsonProperty String description;
    @JsonProperty("alternate") @SerializedName("alternate") List<Link> alternates;
    @JsonProperty long updated;
    @JsonProperty List<Item> items;

    @Override public String toString() {
      StringBuilder result = new StringBuilder()
          .append(id)
          .append("\n").append(title)
          .append("\n").append(description)
          .append("\n").append(alternates)
          .append("\n").append(updated);
      int i = 1;
      for (Item item : items) {
        result.append(i++).append(": ").append(item).append("\n\n");
      }
      return result.toString();
    }
  }

  static class Link {
    @JsonProperty String href;

    @Override public String toString() {
      return href;
    }
  }

  static class Item {
    @JsonProperty List<String> categories;
    @JsonProperty String title;
    @JsonProperty long published;
    @JsonProperty long updated;
    @JsonProperty("alternate") @SerializedName("alternate") List<Link> alternates;
    @JsonProperty Content content;
    @JsonProperty String author;
    @JsonProperty List<ReaderUser> likingUsers;

    @Override public String toString() {
      return title
          + "\nauthor: " + author
          + "\npublished: " + published
          + "\nupdated: " + updated
          + "\n" + content
          + "\nliking users: " + likingUsers
          + "\nalternates: " + alternates
          + "\ncategories: " + categories;
    }
  }

  static class Content {
    @JsonProperty String content;

    @Override public String toString() {
      return content;
    }
  }

  static class ReaderUser {
    @JsonProperty String userId;

    @Override public String toString() {
      return userId;
    }
  }
}
