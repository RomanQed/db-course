package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.dto.TransactionDto;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Account;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.Transaction;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

@JavalinController("/transaction")
public final class TransactionController extends AuthBase {
    private final Repository<Transaction> transactions;
    private final Repository<Currency> categories;
    private final Repository<Account> accounts;

    public TransactionController(JwtProvider<JwtUser> provider,
                                 Repository<User> users,
                                 Repository<Transaction> transactions,
                                 Repository<Currency> categories,
                                 Repository<Account> accounts) {
        super(provider, users);
        this.transactions = transactions;
        this.categories = categories;
        this.accounts = accounts;
    }

    public void get(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = Util.seeOwned(ctx, this, transactions, id);
        if (found == null) {
            return;
        }
        ctx.json(found);
    }

    public void put(Context ctx) {
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
        transactions.put(USER_ROLE, transaction);
        ctx.json(transaction);
    }

    public void delete(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var goal = Util.seeOwned(ctx, this, transactions, id);
        if (goal == null) {
            return;
        }
        transactions.delete(USER_ROLE, goal.getId());
    }
}
