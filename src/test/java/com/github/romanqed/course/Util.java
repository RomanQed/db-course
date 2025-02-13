package com.github.romanqed.course;

import com.github.romanqed.jfunc.Function1;
import com.github.romanqed.jfunc.Runnable1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Date;
import java.util.List;

public final class Util {
    public static final String BUDGETS = "/budget_tools.sql";
    public static final String GOALS = "/goal_tools.sql";
    public static final String TRANSACTIONS = "/transaction_tools.sql";
    public static final String CHECKS = "/check_tools.sql";
    public static final String TABLES = "/tables.sql";
    public static final String USER = "postgres";
    public static final String PASSWORD = "123";
    public static final String URL = "jdbc:postgresql://localhost:5432";
    private static final Class<?> CLASS = Util.class;

    private Util() {
    }

    public static Connection initDatabase(String database, List<String> resources) throws Throwable {
        var master = DriverManager.getConnection(URL + "/?user=" + USER + "&password=" + PASSWORD);
        execute(master, "create database " + database);
        master.close();
        var ret = DriverManager.getConnection(URL + "/" + database + "?user=" + USER + "&password=" + PASSWORD);
        for (var resource : resources) {
            execute(ret, readResource(resource));
        }
        // Insert test user
        update(ret::prepareStatement,
                "insert into users (login, password, admin) values (?, ?, ?);",
                List.of("test", "test", true));
        return ret;
    }

    public static void dropDatabase(String database) throws SQLException {
        var master = DriverManager.getConnection(URL + "/?user=postgres&password=123");
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
