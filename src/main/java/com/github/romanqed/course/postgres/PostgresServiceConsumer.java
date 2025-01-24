package com.github.romanqed.course.postgres;

import com.github.romanqed.course.database.Database;
import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.di.ProviderConsumer;
import com.github.romanqed.course.di.ServiceProviderConsumer;
import com.github.romanqed.course.models.Entity;
import com.github.romanqed.course.util.Util;
import com.github.romanqed.jtype.Types;
import io.github.amayaframework.di.ServiceProvider;
import io.github.amayaframework.di.ServiceProviderBuilder;
import org.atteo.classindex.ClassIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@ProviderConsumer
public final class PostgresServiceConsumer implements ServiceProviderConsumer {
    private static final File POSTGRES_CONFIG = new File("postgres.json");
    private static final String SERVICE_DATABASE = "postgres";
    private static final Class<?> CLASS = PostgresServiceConsumer.class;
    private static final String CHECK_TOOLS = "/sql/check_tools.sql";
    private static final String TABLES = "/sql/tables.sql";
    private static final String ROLES = "/sql/roles.sql";
    private static final String CHECK = "/sql/check.sql";
    private static final String TRANSACTION_TOOLS = "/sql/transaction_tools.sql";
    private static final String BUDGET_TOOLS = "/sql/budget_tools.sql";

    private static Connection initDatabase(PostgresConfig config) throws Exception {
        var url = config.getUrl();
        var service = url + SERVICE_DATABASE;
        var connection = DriverManager.getConnection(service, config.getUser(), config.getPassword());
        var database = config.getDatabase();
        var exists = checkDatabase(connection, database);
        var ret = (Connection) null;
        if (exists) {
            ret = DriverManager.getConnection(url + database, config.getUser(), config.getPassword());
            validateDatabase(ret);
        } else {
            ret = createDatabase(connection, config);
        }
        connection.close();
        return ret;
    }

    private static boolean check(Connection connection, String sql) throws SQLException {
        var statement = connection.createStatement();
        var set = statement.executeQuery(sql);
        if (!set.next()) {
            throw new IllegalStateException("Cannot retrieve check result");
        }
        var ret = set.getBoolean(1);
        statement.close();
        return ret;
    }

    private static boolean checkDatabase(Connection connection, String database) throws SQLException {
        // Check if project database exists
        var sql = "select exists(select datname from pg_catalog.pg_database where lower(datname) = lower('%s'))";
        return check(connection, String.format(sql, database));
    }

    private static void executeSql(Connection connection, String sql) throws SQLException {
        var statement = connection.createStatement();
        statement.execute(sql);
        statement.close();
    }

    private static String readResource(String name) throws IOException {
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

    private static Connection createDatabase(Connection connection, PostgresConfig config) throws Exception {
        var database = config.getDatabase();
        // Create database
        var sql = "create database %s; grant all privileges on database %s to postgres;";
        executeSql(connection, String.format(sql, database, database));
        // Connect to database
        var ret = DriverManager.getConnection(config.getUrl() + database, config.getUser(), config.getPassword());
        // Create functions
        executeSql(ret, readResource(CHECK_TOOLS));
        // Create tables
        executeSql(ret, readResource(TABLES));
        // Create roles
        try {
            executeSql(ret, readResource(ROLES).replace("%database", database));
        } catch (Throwable ignored) {
            // Suppress role exceptions
        }
        // Create functions
        executeSql(ret, readResource(TRANSACTION_TOOLS));
        executeSql(ret, readResource(BUDGET_TOOLS));
        return ret;
    }

    private static void validateDatabase(Connection connection) throws Exception {
        // Check if all tables exists
        var exists = check(connection, readResource(CHECK));
        if (!exists) {
            throw new IllegalStateException("Illegal database state: tables not exists");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pre(ServiceProviderBuilder builder) throws Exception {
        var config = Util.read(POSTGRES_CONFIG, PostgresConfig.class);
        var url = config.getUrl();
        if (!url.endsWith("/")) {
            config.setUrl(url + "/");
        }
        builder.addInstance(PostgresConfig.class, config);
        var connection = initDatabase(config);
        builder.addInstance(Connection.class, connection);
        var found = ClassIndex.getAnnotated(Model.class);
        var database = new PostgresDatabase(connection);
        builder.addInstance(Database.class, database);
        for (var clazz : found) {
            var repository = database.create((Class<? extends Entity>) clazz);
            var type = Types.of(Repository.class, clazz);
            builder.addInstance(type, repository);
        }
    }

    @Override
    public void post(ServiceProvider provider) {
    }
}
