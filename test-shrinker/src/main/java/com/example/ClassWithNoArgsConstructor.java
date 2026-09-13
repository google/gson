package com.example;

import com.google.gson.annotations.SerializedName;

/** Class with no-args constructor and with field annotated with {@link SerializedName}. */
public class ClassWithNoArgsConstructor {
  @SerializedName("myField")
  public int i;

  public ClassWithNoArgsConstructor() {
    i = -3;
  }
}
