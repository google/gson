package com.example;

import com.google.gson.annotations.SerializedName;

/**
 * Class without no-args constructor, but with field annotated with {@link SerializedName}. The
 * constructor should not be used in the code, but this shouldn't lead to R8 concluding that values
 * of the type are not constructible and therefore must be null.
 */
public class ClassWithUnreferencedHasArgsConstructor {
  @SerializedName("myField")
  public int i;

  // Specify explicit constructor with args to remove implicit no-args default constructor
  public ClassWithUnreferencedHasArgsConstructor(int i) {
    this.i = i;
  }
}
