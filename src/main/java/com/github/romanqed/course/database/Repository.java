package com.github.romanqed.course.database;

import com.github.romanqed.course.models.Entity;

import java.util.List;

public interface Repository<V extends Entity> {

    void put(String role, V model);

    void update(String role, V model);

    V get(String role, int key);

    List<V> get(String role, String where);

    List<V> get(String role, String field, Object value);

    V getFirst(String role, String field, Object value);

    List<V> get(String role);

    boolean exists(String role, int id);

    boolean exists(String role, String where);

    boolean exists(String role, String field, Object value);

    void delete(String role, int key);

    void delete(String role, String where);
}
