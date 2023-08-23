package com.example;

import com.google.gson.annotations.SerializedName;

/**
 * Class with no-args default constructor and with field annotated with
 * {@link SerializedName}.
 */
public class ClassWithDefaultConstructor {
  @SerializedName("myField")
  public int i;

  public ClassWithDefaultConstructor() {
    i = -3;
  }
}
