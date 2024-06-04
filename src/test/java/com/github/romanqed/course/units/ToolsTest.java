package com.github.romanqed.course.units;

import com.github.romanqed.course.Util;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ToolsTest {
    static Connection connection;

    @BeforeAll
    public static void initDatabase() throws Throwable {
        connection = Util.initDatabase("exists_test", List.of(
                Util.TABLES,
                Util.CHECKS
        ));
    }

    @AfterAll
    public static void dropDatabase() throws SQLException {
        connection.close();
        Util.dropDatabase("exists_test");
    }

    private boolean checkTable(String table) throws Throwable {
        var ret = new boolean[1];
        Util.query(connection::prepareCall,
                "select check_table_exists(?)",
                List.of(table),
                set -> ret[0] = set.getBoolean(1));
        return ret[0];
    }

    private boolean checkTables(List<String> tables) throws Throwable {
        var ret = new boolean[1];
        var merged = tables
                .stream()
                .reduce("", (p, n) -> p + ",'" + n + "'")
                .substring(1);
        Util.query(connection::prepareCall,
                "select check_tables_exist(array[" + merged + "])",
                List.of(),
                set -> ret[0] = set.getBoolean(1));
        return ret[0];
    }

    @Test
    public void testTableExists() throws Throwable {
        assertTrue(checkTable("users"));
    }

    @Test
    public void testTableNotExists() throws Throwable {
        assertFalse(checkTable("unknowns"));
    }

    @Test
    public void testTablesExist() throws Throwable {
        assertTrue(checkTables(List.of(
                "currencies",
                "exchanges",
                "categories",
                "users",
                "budgets",
                "accounts",
                "goals",
                "transactions"
        )));
    }

    @Test
    public void testTablesNotExist() throws Throwable {
        assertFalse(checkTables(List.of(
                "currencies",
                "unknowns",
                "goals"
        )));
    }
}
