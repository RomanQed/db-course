package com.github.romanqed.course;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.models.Entity;

import java.util.List;

class RepositoryImpl<V extends Entity> implements Repository<V> {

    @Override
    public void put(String role, V model) {

    }

    @Override
    public void update(String role, V model) {

    }

    @Override
    public V get(String role, int key) {
        return null;
    }

    @Override
    public List<V> get(String role, String where) {
        return null;
    }

    @Override
    public List<V> get(String role, String field, Object value) {
        return null;
    }

    @Override
    public V getFirst(String role, String field, Object value) {
        return null;
    }

    @Override
    public List<V> get(String role) {
        return null;
    }

    @Override
    public boolean exists(String role, int id) {
        return false;
    }

    @Override
    public boolean exists(String role, String where) {
        return false;
    }

    @Override
    public boolean exists(String role, String field, Object value) {
        return false;
    }

    @Override
    public void delete(String role, int key) {

    }

    @Override
    public void delete(String role, String where) {

    }
}
