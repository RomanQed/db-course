package com.github.romanqed.course;

import com.github.romanqed.jfunc.Runnable0;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Benchmark {
    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int ITERATIONS = 100;
    private static final String BENCHMARK = "benchmark";
    private static final int CATEGORY = 1;
    private static final int CURRENCY = 1;
    private static final int ACCOUNT = 1;
    private static final int YEAR = 2024;
    private static final String HEADER = "insert into transactions " +
            "(owner,category,_from,_to,value,description,_timestamp) values ";
    private static final String FROM_PATTERN = ",(1,1,1,null,5,'','";
    private static final String TO_PATTERN = ",(1,1,null,1,5,'','";
    private static final Date START = getDate(1, 6);
    private static final Date END = getDate(31, 6);
    private static final String PARTS = "/part.sql";

    public static void main(String[] args) throws Throwable {
        measure(false);
        measure(true);
    }

    static void measure(boolean parts) throws Throwable {
        System.out.println("Measure with parts: " + parts);
        // Init database
        var connection = init(parts);
        try {
            measure(connection, parts);
        } catch (Throwable e) {
            System.out.println("Cannot finish measurement due to " + e.getMessage());
            e.printStackTrace();
        }
        connection.close();
        // Drop database
        destroy(connection);
    }

    private static final int LIMIT = 1_000_000;
    private static final int STEP = 100_000;

    static void measure(Connection connection, boolean parts) throws Throwable {
        var stream = new FileOutputStream("res_parts_" + parts + ".csv");
        var writer = new BufferedWriter(new OutputStreamWriter(stream));
        var random = ThreadLocalRandom.current();
        for (var i = STEP; i <= LIMIT; i += STEP) {
            System.out.println("Do for n = " + i);
            var time = measure(connection, parts, random, i);
            writer.write(i + "," + time);
            writer.newLine();
        }
        writer.flush();
        writer.close();
        stream.close();
    }

    static void putBudget(Connection c) throws Throwable {
        Util.update(c::prepareStatement,
                "insert into budgets (id,owner,currency,_start,_end,description,value) " +
                        "values (1,1,?,?,?,'',0)",
                List.of(CURRENCY, START, END));
    }

    static void getBudgetStatus(Connection c) throws SQLException {
        var statement = c.createStatement();
        statement.execute("select get_budget_status(1,1)");
        statement.close();
    }

    static int count(Connection c, String table) throws Throwable {
        var ret = new int[1];
        Util.query(c::prepareStatement,
                "select count(*) from " + table,
                List.of(),
                set -> ret[0] = set.getInt(1));
        return ret[0];
    }

    static long measure(Connection c, boolean parts, ThreadLocalRandom random, int count) throws Throwable {
        // Put budget
        putBudget(c);
        // Put transactions
        generateRandomTransactions(c, random, count);
        // Print partitioning
        if (parts) {
            System.out.println("Rows in t_part: " + count(c, "t_part"));
            System.out.println("Rows in t_part_others: " + count(c, "t_part_others"));
        }
        // Measure getting budget status
        var ret = measure(() -> getBudgetStatus(c));
        // Reset entities
        Util.execute(c, "delete from budgets");
        Util.execute(c, "delete from transactions");
        return ret;
    }

    static void generateRandomTransactions(Connection c, ThreadLocalRandom random, int count) throws Throwable {
        // Income transactions
        var incomes = generateRandomBatch(TO_PATTERN, random, count / 2);
        // Outcome transactions
        var outcomes = generateRandomBatch(FROM_PATTERN, random, count / 2);
        // Push to database
        Util.execute(c, incomes);
        Util.execute(c, outcomes);
    }

    static Connection init(boolean parts) throws Throwable {
        var ret = Util.initDatabase(BENCHMARK, List.of(Util.TABLES, Util.BUDGETS));
        // Insert stub category
        Util.update(ret::prepareStatement,
                "insert into categories (id,name) values (?,?)",
                List.of(CATEGORY, "cat"));
        // Insert stub currency
        Util.update(ret::prepareStatement,
                "insert into currencies (id,name) values (?,?)",
                List.of(CURRENCY, "cur"));
        // Insert stub account
        TransactionUtil.putAccount(ret, ACCOUNT, CURRENCY, 0);
        if (!parts) {
            return ret;
        }
        // Add partitions
        var script = Util.readResource(PARTS)
                .replace("%start", FORMATTER.format(START))
                .replace("%end", FORMATTER.format(END));
        Util.execute(ret, script);
        return ret;
    }

    static void destroy(Connection c) throws SQLException {
        c.close();
        Util.dropDatabase(BENCHMARK);
    }

    static Date getDate(int day, int month) {
        var ret = Calendar.getInstance();
        ret.setTimeInMillis(0);
        ret.set(Calendar.DAY_OF_MONTH, day);
        ret.set(Calendar.MONTH, month);
        ret.set(Calendar.YEAR, YEAR);
        return ret.getTime();
    }

    static Date getRandomDate(ThreadLocalRandom random) {
        var day = random.nextInt(1, 29);
        var month = random.nextInt(1, 13);
        return getDate(day, month);
    }

    static String generateRandomBatch(String pattern, ThreadLocalRandom random, int count) {
        var builder = new StringBuilder();
        for (var i = 0; i < count; ++i) {
            var date = getRandomDate(random);
            builder
                    .append(pattern)
                    .append(FORMATTER.format(date))
                    .append("')");
        }
        return HEADER + builder.substring(1);
    }

    static long measure(Runnable0 runnable) throws Throwable {
        var start = System.nanoTime();
        for (var i = 0; i < ITERATIONS; ++i) {
            runnable.run();
        }
        return (System.nanoTime() - start) / ITERATIONS;
    }
}
