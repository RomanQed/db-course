package com.github.romanqed.course;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class BudgetTest {
    static Connection connection;

    @BeforeAll
    public static void initDatabase() throws Throwable {
        connection = Util.initDatabase("budget_test", List.of(
                Util.TABLES,
                Util.BUDGETS
        ));
    }

    @AfterAll
    public static void dropDatabase() throws SQLException {
        connection.close();
        Util.dropDatabase("budget_test");
    }


}
