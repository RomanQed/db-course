package com.github.romanqed.course;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TransactionTest {
    private static final int RUBLES = 1;
    private static final int DOLLARS = 2;

    static Connection connection;

    @BeforeAll
    public static void initDatabase() throws Throwable {
        connection = Util.initDatabase("transaction_test", List.of(
                Util.TABLES,
                Util.TRANSACTIONS
        ));
        // Insert test category
        Util.update(connection::prepareStatement, "insert into categories (name) values (?)", List.of("test"));
        // Insert test currencies and exchange for it
        Util.update(connection::prepareStatement,
                "insert into currencies (id,name) values (?,?)",
                List.of(RUBLES, "rub"));
        Util.update(connection::prepareStatement,
                "insert into currencies (id,name) values (?,?)",
                List.of(DOLLARS, "usd"));
        Util.update(connection::prepareStatement,
                "insert into exchanges (_from,_to,factor) values (?,?,?)",
                List.of(DOLLARS, RUBLES, 100));
        Util.update(connection::prepareStatement,
                "insert into exchanges (_from,_to,factor) values (?,?,?)",
                List.of(RUBLES, DOLLARS, 0.01));
    }

    @AfterAll
    public static void dropDatabase() throws SQLException {
        connection.close();
        Util.dropDatabase("transaction_test");
    }

    private int putTransaction(Integer from, Integer to, double value) throws Throwable {
        var ret = new int[1];
        var query = "select add_transaction(?,?,%from,%to,?,?,?)";
        Util.query(connection::prepareCall,
                query
                        .replace("%from", from == null ? "null" : from.toString())
                        .replace("%to", to == null ? "null" : to.toString()),
                List.of(1, 1, value, "", new Date()),
                set -> ret[0] = set.getInt(1));
        return ret[0];
    }

    private double getAccountValue(int id) throws Throwable {
        var ret = new double[1];
        Util.query(connection::prepareStatement,
                "select value from accounts where id = ?",
                List.of(id),
                set -> ret[0] = set.getDouble(1));
        return ret[0];
    }

    private void deleteTransaction(int id) throws Throwable {
        Util.update(connection::prepareCall, "call del_transaction(?)", List.of(id));
    }

    private void putAccount(int id, int currency, double value) throws Throwable {
        Util.update(connection::prepareStatement,
                "insert into accounts (id, owner, currency, description, value) values (?,?,?,?,?)",
                List.of(id, 1, currency, "", value));
    }

    private void deleteAccount(int id) throws Throwable {
        Util.update(connection::prepareStatement, "delete from accounts where id = ?", List.of(id));
    }

    private void addAccountValue(int id, int currency, double value) throws Throwable {
        Util.update(connection::prepareCall, "call add_account_value(?,?,?)", List.of(id, currency, value));
    }

    @Test
    public void testAddSameAccountValue() throws Throwable {
        putAccount(1, RUBLES, 0);
        addAccountValue(1, RUBLES, 100);
        assertEquals(100, getAccountValue(1));
        deleteAccount(1);
    }

    @Test
    public void testAddDiffAccountValue() throws Throwable {
        putAccount(1, RUBLES, 0);
        addAccountValue(1, DOLLARS, 1);
        assertEquals(100, getAccountValue(1));
        deleteAccount(1);
    }

    @Test
    public void testSameInner() throws Throwable {
        putAccount(1, RUBLES, 100);
        putAccount(2, RUBLES, 0);
        var id = putTransaction(1, 2, 100);
        assertEquals(0, getAccountValue(1));
        assertEquals(100, getAccountValue(2));
        deleteTransaction(id);
        assertEquals(100, getAccountValue(1));
        assertEquals(0, getAccountValue(2));
        deleteAccount(1);
        deleteAccount(2);
    }

    @Test
    public void testDiffInner() throws Throwable {
        putAccount(1, RUBLES, 100);
        putAccount(2, DOLLARS, 0);
        var id = putTransaction(1, 2, 100);
        assertEquals(0, getAccountValue(1));
        assertEquals(1, getAccountValue(2));
        deleteTransaction(id);
        assertEquals(100, getAccountValue(1));
        assertEquals(0, getAccountValue(2));
        deleteAccount(1);
        deleteAccount(2);
    }

    @Test
    public void testToInner() throws Throwable {
        putAccount(1, RUBLES, 10);
        var id = putTransaction(null, 1, 50);
        assertEquals(60, getAccountValue(1));
        deleteTransaction(id);
        assertEquals(10, getAccountValue(1));
        deleteAccount(1);
    }

    @Test
    public void testFromInner() throws Throwable {
        putAccount(1, RUBLES, 88);
        var id = putTransaction(1, null, 20);
        assertEquals(68, getAccountValue(1));
        deleteTransaction(id);
        assertEquals(88, getAccountValue(1));
        deleteAccount(1);
    }
}
