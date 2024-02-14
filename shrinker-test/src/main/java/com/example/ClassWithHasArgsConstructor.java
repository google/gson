package com.example;

import com.google.gson.annotations.SerializedName;

/** Class without no-args constructor, but with field annotated with {@link SerializedName}. */
public class ClassWithHasArgsConstructor {
  @SerializedName("myField")
  public int i;

  // Specify explicit constructor with args to remove implicit no-args default constructor
  public ClassWithHasArgsConstructor(int i) {
    this.i = i;
  }
}
