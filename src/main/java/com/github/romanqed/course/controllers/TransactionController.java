package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.dto.TransactionDto;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.*;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

@JavalinController("/transaction")
public final class TransactionController extends AuthBase {
    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Connection connection;
    private final Repository<Transaction> transactions;
    private final Repository<Category> categories;
    private final Repository<Account> accounts;

    public TransactionController(JwtProvider<JwtUser> provider,
                                 Connection connection,
                                 Repository<User> users,
                                 Repository<Transaction> transactions,
                                 Repository<Category> categories,
                                 Repository<Account> accounts) {
        super(provider, users);
        this.connection = connection;
        this.transactions = transactions;
        this.categories = categories;
        this.accounts = accounts;
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = Util.seeOwned(ctx, this, transactions, id);
        if (found == null) {
            return;
        }
        ctx.json(found);
    }

    private void put(Transaction transaction) throws SQLException {
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
        var statement = connection.createStatement();
        statement.execute(formatted);
        statement.getMoreResults(); // Skip set role
        var set = statement.getResultSet();
        if (!set.next()) {
            throw new IllegalStateException("Cannot retrieve transaction id");
        }
        transaction.setId(set.getInt(1));
        statement.close();
    }

    @Route(method = HandlerType.POST, route = "/")
    public void post(Context ctx) throws SQLException {
        var dto = DtoUtil.validate(ctx, TransactionDto.class);
        if (dto == null) {
            return;
        }
        var user = getCheckedUser(ctx);
        if (user == null) {
            return;
        }
        var category = dto.getCategory();
        if (!categories.exists(USER_ROLE, category)) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        var from = dto.getFrom();
        if (from != null && !accounts.exists(USER_ROLE, from)) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        var to = dto.getTo();
        if (to != null && !accounts.exists(USER_ROLE, to)) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        var transaction = Transaction.of(user.getId(), category, dto.getValue());
        transaction.setFrom(from);
        transaction.setTo(to);
        transaction.setDescription(Objects.requireNonNullElse(dto.getDescription(), ""));
        put(transaction);
        ctx.json(transaction);
    }

    private void delete(int id) throws SQLException {
        var sql = "call del_transaction(" + id + ")";
        var statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var transaction = Util.seeOwned(ctx, this, transactions, id);
        if (transaction == null) {
            return;
        }
        delete(transaction.getId());
    }
}
