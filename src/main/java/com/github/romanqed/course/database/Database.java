package com.github.romanqed.course.database;

import com.github.romanqed.course.models.Entity;

public interface Database {

    <V extends Entity> Repository<V> create(Class<V> type);

    void close();
}
