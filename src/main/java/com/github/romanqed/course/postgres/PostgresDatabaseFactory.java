package com.github.romanqed.course.postgres;

import com.github.romanqed.course.database.Database;
import com.github.romanqed.course.database.DatabaseFactory;
import com.github.romanqed.jfunc.Exceptions;

import java.sql.DriverManager;

public final class PostgresDatabaseFactory implements DatabaseFactory {

    @Override
    public Database create(String url) {
        var connection = Exceptions.suppress(() -> DriverManager.getConnection(url));
        return new PostgresDatabase(connection);
    }
}
