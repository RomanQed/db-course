package com.github.romanqed.course;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

final class TransactionUtil {
    static final int RUBLES = 1;
    static final int DOLLARS = 2;

    private TransactionUtil() {
    }

    static void init(Connection c) throws Throwable {
        // Insert test category
        Util.update(c::prepareStatement, "insert into categories (name) values (?)", List.of("test"));
        // Insert test currencies and exchange for it
        Util.update(c::prepareStatement,
                "insert into currencies (id,name) values (?,?)",
                List.of(RUBLES, "rub"));
        Util.update(c::prepareStatement,
                "insert into currencies (id,name) values (?,?)",
                List.of(DOLLARS, "usd"));
        Util.update(c::prepareStatement,
                "insert into exchanges (_from,_to,factor) values (?,?,?)",
                List.of(DOLLARS, RUBLES, 100));
        Util.update(c::prepareStatement,
                "insert into exchanges (_from,_to,factor) values (?,?,?)",
                List.of(RUBLES, DOLLARS, 0.01));
    }

    static int putTransaction(Connection c, Integer from, Integer to, double value, Date date) throws Throwable {
        var ret = new int[1];
        var query = "select add_transaction(?,?,%from,%to,?,?,?)";
        Util.query(c::prepareCall,
                query
                        .replace("%from", from == null ? "null" : from.toString())
                        .replace("%to", to == null ? "null" : to.toString()),
                List.of(1, 1, value, "", date),
                set -> ret[0] = set.getInt(1));
        return ret[0];
    }

    static int putTransaction(Connection c, Integer from, Integer to, double value) throws Throwable {
        return putTransaction(c, from, to, value, new Date());
    }

    static double getAccountValue(Connection c, int id) throws Throwable {
        var ret = new double[1];
        Util.query(c::prepareStatement,
                "select value from accounts where id = ?",
                List.of(id),
                set -> ret[0] = set.getDouble(1));
        return ret[0];
    }

    static void deleteTransaction(Connection c, int id) throws Throwable {
        Util.update(c::prepareCall, "call del_transaction(?)", List.of(id));
    }

    static void putAccount(Connection c, int id, int currency, double value) throws Throwable {
        Util.update(c::prepareStatement,
                "insert into accounts (id, owner, currency, description, value) values (?,?,?,?,?)",
                List.of(id, 1, currency, "", value));
    }

    static void deleteAccount(Connection c, int id) throws Throwable {
        Util.update(c::prepareStatement, "delete from accounts where id = ?", List.of(id));
    }
}
