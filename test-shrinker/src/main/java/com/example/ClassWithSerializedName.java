package com.example;

import com.google.gson.annotations.SerializedName;

public class ClassWithSerializedName {
  @SerializedName("myField")
  public int i;

  @SerializedName("notAccessed")
  public short notAccessedField = -1;

  public ClassWithSerializedName(int i) {
    this.i = i;
  }
}
