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
package com.google.gson.rest.definition;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.rest.definition.Id;

/**
 * Unit tests for {@link Id.GsonTypeAdapter}
 *
 * @author inder
 */
public class IdTypeAdapterTest extends TestCase {
  private static final Id<Student> STUDENT1_ID = Id.get(5L, Student.class);
  private static final Id<Student> STUDENT2_ID = Id.get(6L, Student.class);
  private static final Student STUDENT1 = new Student(STUDENT1_ID, "first");
  private static final Student STUDENT2 = new Student(STUDENT2_ID, "second");
  private static final Type TYPE_COURSE_HISTORY =
    new TypeToken<Course<HistoryCourse>>(){}.getType(); 
  private static final Id<Course<HistoryCourse>> COURSE_ID = Id.get(10L, TYPE_COURSE_HISTORY);

  private Gson gson;
  private Course<HistoryCourse> course;

  @Override
  protected void setUp() {
    gson = new GsonBuilder()
        .registerTypeAdapter(Id.class, new Id.GsonTypeAdapter())
        .create();
    course = new Course<HistoryCourse>(COURSE_ID, 4,
        new Assignment<HistoryCourse>(null, null), createList(STUDENT1, STUDENT2));
  }

  public void testSerializeId() {
    String json = gson.toJson(course, TYPE_COURSE_HISTORY);
    assertTrue(json.contains(String.valueOf(COURSE_ID.getValue())));
    assertTrue(json.contains(String.valueOf(STUDENT1_ID.getValue())));
    assertTrue(json.contains(String.valueOf(STUDENT2_ID.getValue())));
  }

  public void testDeserializeId() {
    String json = "{courseId:1,students:[{id:1,name:'first'},{id:6,name:'second'}],"
      + "numAssignments:4,assignment:{}}";
    Course<HistoryCourse> target = gson.fromJson(json, TYPE_COURSE_HISTORY);
    assertEquals(1, target.getStudents().get(0).id.getValue());
    assertEquals(6, target.getStudents().get(1).id.getValue());
    assertEquals(1, target.getId().getValue());
  }

  @SuppressWarnings("unused")
  private static class Student {
    Id<Student> id;
    String name;

    private Student() {
      this(null, null);
    }
    public Student(Id<Student> id, String name) {
      this.id = id;
      this.name = name;
    }
  }
  @SuppressWarnings("unused")
  private static class Course<T> {
    final List<Student> students;
    private final Id<Course<T>> courseId;
    private final int numAssignments;
    private final Assignment<T> assignment;

    private Course() {
      this(null, 0, null, new ArrayList<Student>());
    }

    public Course(Id<Course<T>> courseId, int numAssignments,
        Assignment<T> assignment, List<Student> players) {
      this.courseId = courseId;
      this.numAssignments = numAssignments;
      this.assignment = assignment;
      this.students = players;
    }
    public Id<Course<T>> getId() {
      return courseId;
    }
    List<Student> getStudents() {
      return students;
    }
  }

  @SuppressWarnings("unused")
  private static class Assignment<T> {
    private final Id<Assignment<T>> id;
    private final T data;

    private Assignment() {
      this(null, null);
    }
    public Assignment(Id<Assignment<T>> id, T data) {
      this.id = id;
      this.data = data;
    }
  }

  @SuppressWarnings("unused")
  private static class HistoryCourse {
    int numClasses;
  }

  private static <T> List<T> createList(T ...items) {
    return Arrays.asList(items);
  }
}
