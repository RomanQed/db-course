package com.github.romanqed.course.postgres;

public interface Deserializer {

    void deserialize(Getter getter, Object object);
}
