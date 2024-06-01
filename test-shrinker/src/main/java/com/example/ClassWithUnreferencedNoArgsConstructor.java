package com.example;

import com.google.gson.annotations.SerializedName;

/**
 * Class with no-args constructor and with field annotated with {@link SerializedName}. The
 * constructor should not be used in the code, but this shouldn't lead to R8 concluding that values
 * of the type are not constructible and therefore must be null.
 */
public class ClassWithUnreferencedNoArgsConstructor {
  @SerializedName("myField")
  public int i;

  public ClassWithUnreferencedNoArgsConstructor() {
    i = -3;
  }
}
