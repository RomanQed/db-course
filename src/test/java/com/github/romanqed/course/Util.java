package com.github.romanqed.course;

import com.github.romanqed.jfunc.Function1;
import com.github.romanqed.jfunc.Runnable1;
import org.h2.jdbcx.JdbcDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Date;
import java.util.List;

public final class Util {
    public static final String TABLES = "/tables.sql";
    private static final Class<?> CLASS = Util.class;

    private Util() {
    }

    public static Connection initDatabase(JdbcDataSource ds, List<String> resources) throws Throwable {
        var ret = ds.getConnection();
        for (var resource : resources) {
            execute(ret, readResource(resource));
        }
        // Insert test user
        update(ret::prepareStatement,
                "insert into users (login, password, admin) values (?, ?, ?);",
                List.of("test", "test", true));
        return ret;
    }

    public static void dropDatabase(JdbcDataSource ds, String database) throws SQLException {
        var master = ds.getConnection();
        execute(master, "drop database " + database);
        master.close();
    }

    public static void execute(Connection c, String sql) throws SQLException {
        var statement = c.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    public static void query(Function1<String, PreparedStatement> f,
                             String sql,
                             List<Object> args,
                             Runnable1<ResultSet> consumer) throws Throwable {
        var statement = f.invoke(sql);
        var i = 1;
        for (var arg : args) {
            if (arg instanceof Date) {
                statement.setObject(i++, arg, Types.TIMESTAMP);
            } else {
                statement.setObject(i++, arg);
            }
        }
        var set = statement.executeQuery();
        if (!set.next()) {
            throw new IllegalStateException();
        }
        consumer.run(set);
        statement.close();
    }

    public static void update(Function1<String, PreparedStatement> f, String sql, List<Object> args) throws Throwable {
        var statement = f.invoke(sql);
        var i = 1;
        for (var arg : args) {
            if (arg instanceof Date) {
                statement.setObject(i++, arg, Types.TIMESTAMP);
            } else {
                statement.setObject(i++, arg);
            }
        }
        statement.executeUpdate();
        statement.close();
    }

    public static String readResource(String name) throws IOException {
        var stream = CLASS.getResourceAsStream(name);
        if (stream == null) {
            throw new IllegalStateException("Resource not found");
        }
        var reader = new BufferedReader(new InputStreamReader(stream));
        var ret = reader.lines().reduce("", (p, n) -> p + n + "\n");
        reader.close();
        stream.close();
        return ret;
    }
}
