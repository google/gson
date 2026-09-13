package com.example;

public class ClassWithNamedFields {
  public int myField;
  public short notAccessedField = -1;

  public ClassWithNamedFields(int i) {
    myField = i;
  }
}
