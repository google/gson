package com.example;

import com.google.gson.annotations.SerializedName;

/**
 * Class with no-args constructor and field annotated with {@code @SerializedName}, but which is not
 * actually used anywhere in the code.
 *
 * <p>The default ProGuard rules should not keep this class.
 */
public class UnusedClass {
  public UnusedClass() {}

  @SerializedName("i")
  public int i;
}
