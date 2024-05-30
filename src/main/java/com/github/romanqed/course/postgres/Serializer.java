package com.github.romanqed.course.postgres;

public interface Serializer {

    void serialize(Setter setter, Object object);
}
