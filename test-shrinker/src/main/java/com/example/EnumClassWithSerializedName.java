package com.example;

import com.google.gson.annotations.SerializedName;

public enum EnumClassWithSerializedName {
  @SerializedName("one")
  FIRST,
  @SerializedName("two")
  SECOND
}
