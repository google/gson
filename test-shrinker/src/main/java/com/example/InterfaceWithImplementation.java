package com.example;

import com.google.gson.annotations.SerializedName;

/** Interface whose implementation class is only referenced for deserialization */
public interface InterfaceWithImplementation {
  String getValue();

  // Implementation class which is only referenced in `TypeToken`, but nowhere else
  public static class Implementation implements InterfaceWithImplementation {
    public Implementation() {}

    @SerializedName("s")
    public String s;

    @Override
    public String getValue() {
      return s;
    }
  }
}
