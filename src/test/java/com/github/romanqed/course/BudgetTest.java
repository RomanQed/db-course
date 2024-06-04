package com.github.romanqed.course;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.github.romanqed.course.TransactionUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class BudgetTest {
    private static final Date DATE = getDate(1, 3);
    private static final Date START = getDate(1, 1);
    private static final Date END = getDate(31, 12);
    static Connection connection;

    @BeforeAll
    public static void initDatabase() throws Throwable {
        connection = Util.initDatabase("budget_test", List.of(
                Util.TABLES,
                Util.TRANSACTIONS,
                Util.BUDGETS
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
        Util.dropDatabase("budget_test");
    }

    private static Date getDate(int day, int month) {
        var calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2024);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }

    private double convertValue(int from, int to, double value) throws Throwable {
        var ret = new double[1];
        Util.query(connection::prepareCall,
                "select convert_value(?,?,?)",
                List.of(from, to, value),
                set -> ret[0] = set.getDouble(1));
        return ret[0];
    }

    @Test
    public void testConvertToSelf() throws Throwable {
        assertEquals(1, convertValue(RUBLES, RUBLES, 1));
        assertEquals(2, convertValue(DOLLARS, DOLLARS, 2));
    }

    @Test
    public void testConvert() throws Throwable {
        assertEquals(100, convertValue(DOLLARS, RUBLES, 1));
        assertEquals(1, convertValue(RUBLES, DOLLARS, 100));
    }

    private String getBudgetStatus(int id) throws Throwable {
        var ret = new String[1];
        Util.query(connection::prepareCall,
                "select get_budget_status(?,?)",
                List.of(id, 1),
                set -> ret[0] = ((PGobject) set.getObject(1)).getValue());
        return ret[0];
    }

    private void putBudget(int id, int currency, Date start, Date end, double value) throws Throwable {
        Util.update(connection::prepareStatement,
                "insert into budgets (id,owner,currency,_start,_end,description,value) " +
                        "values (?,?,?,?,?,?,?)",
                List.of(id, 1, currency, start, end, "", value));
    }

    private void deleteBudget(int id) throws Throwable {
        Util.update(connection::prepareStatement,
                "delete from budgets where id = ?",
                List.of(id));
    }

    private void assertBudget(int id, double spent, double got, double total) throws Throwable {
        var status = getBudgetStatus(id)
                .replace("(", "")
                .replace(")", "")
                .split(",");
        assertEquals(spent, Double.parseDouble(status[0]));
        assertEquals(got, Double.parseDouble(status[1]));
        assertEquals(total, Double.parseDouble(status[2]));
    }

    @Test
    public void testEmptyBudget() throws Throwable {
        putBudget(1, RUBLES, START, END, 0);
        assertBudget(1, 0, 0, 0);
        deleteBudget(1);
    }

    @Test
    public void testPositiveBudget() throws Throwable {
        putAccount(connection, 1, RUBLES, 0);
        putAccount(connection, 2, DOLLARS, 0);
        putTransaction(connection, null, 1, 1000, DATE);
        putTransaction(connection, null, 2, 100, DATE);
        putBudget(1, RUBLES, START, END, 0);
        assertBudget(1, 0, 11000, 11000);
        deleteBudget(1);
        deleteAccount(connection, 1);
        deleteAccount(connection, 2);
    }

    @Test
    public void testNegativeBudget() throws Throwable {
        putAccount(connection, 1, RUBLES, 0);
        putAccount(connection, 2, DOLLARS, 0);
        putTransaction(connection, 1, null, 1000, DATE);
        putTransaction(connection, 2, null, 100, DATE);
        putBudget(1, RUBLES, START, END, 0);
        assertBudget(1, 11000, 0, -11000);
        deleteBudget(1);
        deleteAccount(connection, 1);
        deleteAccount(connection, 2);
    }

    @Test
    public void testBudget() throws Throwable {
        putAccount(connection, 1, RUBLES, 0);
        putAccount(connection, 2, DOLLARS, 0);
        // +1000 rub; +100 usd; got 11k rub
        putTransaction(connection, null, 1, 1000, DATE);
        putTransaction(connection, null, 2, 100, DATE);
        // -800 rub; -86 usd; spent 9400 rub
        putTransaction(connection, 1, null, 800, DATE);
        putTransaction(connection, 2, null, 86, DATE);
        // total 1600
        putBudget(1, RUBLES, START, END, 0);
        assertBudget(1, 9400, 11000, 1600);
        deleteBudget(1);
        deleteAccount(connection, 1);
        deleteAccount(connection, 2);
    }

    @Test
    public void testTransactionsOutOfRange() throws Throwable {
        putAccount(connection, 1, RUBLES, 0);
        // Got 100 rub in january
        putTransaction(connection, null, 1, 100, getDate(2, 1));
        // Spend 80 rub in january
        putTransaction(connection, 1, null, 80, getDate(2, 1));
        // Got 100 rub in august
        putTransaction(connection, null, 1, 100, getDate(1, 8));
        // Spend 20 rub in august
        putTransaction(connection, 1, null, 20, getDate(2, 8));
        // Plan budget june-august
        putBudget(1, RUBLES, getDate(1, 6), getDate(31, 8), 0);
        assertBudget(1, 20, 100, 80);
        deleteBudget(1);
        deleteAccount(connection, 1);
    }
}
