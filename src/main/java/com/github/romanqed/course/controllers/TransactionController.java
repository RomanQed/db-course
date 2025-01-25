package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.dto.TransactionDto;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Account;
import com.github.romanqed.course.models.Category;
import com.github.romanqed.course.models.Transaction;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

@JavalinController("/transactions")
public final class TransactionController extends AuthBase {
    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Connection connection;
    private final Repository<Transaction> transactions;
    private final Repository<Category> categories;
    private final Repository<Account> accounts;
    private final Tracer tracer;

    public TransactionController(JwtProvider<JwtUser> provider,
                                 Connection connection,
                                 Repository<User> users,
                                 Repository<Transaction> transactions,
                                 Repository<Category> categories,
                                 Repository<Account> accounts,
                                 OpenTelemetry telemetry) {
        super(provider, users);
        this.connection = connection;
        this.transactions = transactions;
        this.categories = categories;
        this.accounts = accounts;
        this.tracer = telemetry.getTracer("com.github.romanqed.course.controllers.TransactionController");
    }

    private Span startSpan(String name, Context ctx) {
        return tracer.spanBuilder("TransactionController#" + name)
                .setAttribute("http.method", ctx.method().toString())
                .setAttribute("http.route", ctx.path())
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var span = startSpan("get", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = Util.seeOwned(ctx, this, transactions, id, span);
        if (found == null) {
            span.end();
            return;
        }
        ctx.json(found);
        span.addEvent("FoundTransaction");
        span.end();
    }

    private void put(Transaction transaction, Span span) throws SQLException {
        var sql = "set role %s; select add_transaction(%d,%d,%s,%s,%s,'%s','%s')";
        var from = transaction.getFrom();
        var to = transaction.getTo();
        var formatted = String.format(sql,
                USER_ROLE,
                transaction.getOwner(),
                transaction.getCategory(),
                from == null ? "null" : from.toString(),
                to == null ? "null" : to.toString(),
                transaction.getValue(),
                transaction.getDescription(),
                FORMATTER.format(transaction.getTimestamp()));
        span.addEvent("PreparedSqlQuery", Attributes.builder()
                .put("query", formatted)
                .build()
        );
        try {
            var statement = connection.createStatement();
            statement.execute(formatted);
            statement.getMoreResults(); // Skip set role
            var set = statement.getResultSet();
            if (!set.next()) {
                span.addEvent("TransactionPutFailed");
                span.end();
                throw new IllegalStateException("Cannot retrieve transaction id");
            }
            transaction.setId(set.getInt(1));
            statement.close();
        } catch (SQLException e) {
            span.addEvent("SqlExceptionOccurred", Attributes.builder()
                    .put("exception", e.toString())
                    .build()
            );
            span.end();
            throw e;
        }
    }

    @Route(method = HandlerType.POST, route = "/")
    public void post(Context ctx) throws SQLException {
        var span = startSpan("post", ctx);
        var dto = DtoUtil.validate(ctx, TransactionDto.class, span);
        if (dto == null) {
            span.end();
            return;
        }
        var user = getCheckedUser(ctx);
        if (user == null) {
            span.addEvent("Unauthorized");
            span.end();
            return;
        }
        var category = dto.getCategory();
        if (!categories.exists(USER_ROLE, category)) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("MissingCategory");
            span.end();
            return;
        }
        var from = dto.getFrom();
        if (from != null && !accounts.exists(USER_ROLE, from)) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("MissingFromAccount");
            span.end();
            return;
        }
        var to = dto.getTo();
        if (to != null && !accounts.exists(USER_ROLE, to)) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("MissingToAccount");
            span.end();
            return;
        }
        var transaction = Transaction.of(user.getId(), category, dto.getValue());
        transaction.setFrom(from);
        transaction.setTo(to);
        transaction.setDescription(Objects.requireNonNullElse(dto.getDescription(), ""));
        put(transaction, span);
        ctx.json(transaction);
        span.addEvent("TransactionAdded");
        span.end();
    }

    private void delete(int id, Span span) throws SQLException {
        var sql = "call del_transaction(" + id + ")";
        span.addEvent("PreparedSqlQuery", Attributes.builder()
                .put("query", sql)
                .build()
        );
        try {
            var statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            span.addEvent("SqlExceptionOccurred", Attributes.builder()
                    .put("exception", e.toString())
                    .build()
            );
            span.end();
            throw e;
        }
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) throws SQLException {
        var span = startSpan("delete", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var transaction = Util.seeOwned(ctx, this, transactions, id, span);
        if (transaction == null) {
            span.end();
            return;
        }
        delete(transaction.getId(), span);
        span.addEvent("TransactionDeleted");
        span.end();
    }
}
