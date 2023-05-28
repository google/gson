package com.example;

import com.google.gson.annotations.SerializedName;

public class ClassWithDefaultConstructor {
  @SerializedName("myField")
  public int i;

  public ClassWithDefaultConstructor() {
    i = -3;
  }
}
