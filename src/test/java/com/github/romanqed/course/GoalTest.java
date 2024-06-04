package com.github.romanqed.course;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class GoalTest {
    static Connection connection;

    @BeforeAll
    public static void initDatabase() throws Throwable {
        connection = Util.initDatabase("goal_test", List.of(
                Util.TABLES,
                Util.GOALS
        ));
        // Insert test currency
        Util.update(connection::prepareStatement, "insert into currencies (name) values (?)", List.of("test"));
    }

    @AfterAll
    public static void dropDatabase() throws SQLException {
        connection.close();
        Util.dropDatabase("goal_test");
    }

    @Test
    public void testUnreached() throws Throwable {
        Util.update(connection::prepareStatement,
                "insert into accounts (id, owner, currency, description, value) values (?,?,?,?,?)",
                List.of(1, 1, 1, "", 3438));
        Util.update(connection::prepareStatement,
                "insert into goals (id, owner, account, description, target) values (?,?,?,?,?)",
                List.of(1, 1, 1, "", 10000));
        // 3438 from 8788 ~ 34.38%
        // reached = 3438
        // remained = 6562
        Util.query(connection::prepareCall, "select get_goal_status(?)", List.of(1), set -> {
            var res = (PGobject) set.getObject(1);
            assertEquals("(34.38,3438,6562)", res.getValue());
        });
    }

    @Test
    public void testReached() throws Throwable {
        Util.update(connection::prepareStatement,
                "insert into accounts (id, owner, currency, description, value) values (?,?,?,?,?)",
                List.of(2, 1, 1, "", 9744));
        Util.update(connection::prepareStatement,
                "insert into goals (id, owner, account, description, target) values (?,?,?,?,?)",
                List.of(2, 1, 2, "", 395));
        // 100%
        // reached = 395
        // remained = 0
        Util.query(connection::prepareCall, "select get_goal_status(?)", List.of(2), set -> {
            var res = (PGobject) set.getObject(1);
            assertEquals("(100,395,0)", res.getValue());
        });
    }
}
