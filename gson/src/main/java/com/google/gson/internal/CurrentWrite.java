package com.google.gson.internal;

public class CurrentWrite implements CharSequence{
    private char[] chars;
    private String cachedString;

    void setChars(char[] chars) {
        this.chars = chars;
        this.cachedString = null;
    }

    @Override
    public int length() {
        return chars.length;
    }

    @Override
    public char charAt(int i) {
        return chars[i];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new String(chars, start, end - start);
    }

    // Must return string representation to satisfy toString() contract
    @Override
    public String toString() {
        if (cachedString == null) {
            cachedString = new String(chars);
        }
        return cachedString;
    }
}