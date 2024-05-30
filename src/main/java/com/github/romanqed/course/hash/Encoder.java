package com.github.romanqed.course.hash;

public interface Encoder {
    String encode(String value);

    boolean matches(String raw, String encoded);
}
