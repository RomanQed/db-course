package com.github.romanqed.course;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.github.romanqed.course.TransactionUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TransactionTest {
    static Connection connection;

    @BeforeAll
    public static void initDatabase() throws Throwable {
        connection = Util.initDatabase("transaction_test", List.of(
                Util.TABLES,
                Util.TRANSACTIONS
        ));
        init(connection);
    }

    @AfterAll
    public static void dropDatabase() throws SQLException {
        connection.close();
        Util.dropDatabase("transaction_test");
    }

    private void addAccountValue(int id, int currency, double value) throws Throwable {
        Util.update(connection::prepareCall, "call add_account_value(?,?,?)", List.of(id, currency, value));
    }

    @Test
    public void testAddSameAccountValue() throws Throwable {
        putAccount(connection, 1, RUBLES, 0);
        addAccountValue(1, RUBLES, 100);
        assertEquals(100, getAccountValue(connection, 1));
        deleteAccount(connection, 1);
    }

    @Test
    public void testAddDiffAccountValue() throws Throwable {
        putAccount(connection, 1, RUBLES, 0);
        addAccountValue(1, DOLLARS, 1);
        assertEquals(100, getAccountValue(connection, 1));
        deleteAccount(connection, 1);
    }

    @Test
    public void testSameInner() throws Throwable {
        putAccount(connection, 1, RUBLES, 100);
        putAccount(connection, 2, RUBLES, 0);
        var id = putTransaction(connection, 1, 2, 100);
        assertEquals(0, getAccountValue(connection, 1));
        assertEquals(100, getAccountValue(connection, 2));
        deleteTransaction(connection, id);
        assertEquals(100, getAccountValue(connection, 1));
        assertEquals(0, getAccountValue(connection, 2));
        deleteAccount(connection, 1);
        deleteAccount(connection, 2);
    }

    @Test
    public void testDiffInner() throws Throwable {
        putAccount(connection, 1, RUBLES, 100);
        putAccount(connection, 2, DOLLARS, 0);
        var id = putTransaction(connection, 1, 2, 100);
        assertEquals(0, getAccountValue(connection, 1));
        assertEquals(1, getAccountValue(connection, 2));
        deleteTransaction(connection, id);
        assertEquals(100, getAccountValue(connection, 1));
        assertEquals(0, getAccountValue(connection, 2));
        deleteAccount(connection, 1);
        deleteAccount(connection, 2);
    }

    @Test
    public void testToInner() throws Throwable {
        putAccount(connection, 1, RUBLES, 10);
        var id = putTransaction(connection, null, 1, 50);
        assertEquals(60, getAccountValue(connection, 1));
        deleteTransaction(connection, id);
        assertEquals(10, getAccountValue(connection, 1));
        deleteAccount(connection, 1);
    }

    @Test
    public void testFromInner() throws Throwable {
        putAccount(connection, 1, RUBLES, 88);
        var id = putTransaction(connection, 1, null, 20);
        assertEquals(68, getAccountValue(connection, 1));
        deleteTransaction(connection, id);
        assertEquals(88, getAccountValue(connection, 1));
        deleteAccount(connection, 1);
    }
}
