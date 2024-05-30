package com.github.romanqed.course.database;

public interface DatabaseFactory {

    Database create(String url);
}
