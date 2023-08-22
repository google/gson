package com.example;

import com.google.gson.annotations.SerializedName;

/**
 * Class without no-args default constructor, but with field annotated with
 * {@link SerializedName}.
 */
public class ClassWithoutDefaultConstructor {
  @SerializedName("myField")
  public int i;

  // Specify explicit constructor with args to remove implicit no-args default constructor
  public ClassWithoutDefaultConstructor(int i) {
    this.i = i;
  }
}
