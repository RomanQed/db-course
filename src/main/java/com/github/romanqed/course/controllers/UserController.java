package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.Credentials;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.hash.Encoder;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.*;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

@JavalinController("/user")
public final class UserController extends AuthBase {
    private final Repository<Account> accounts;
    private final Repository<Budget> budgets;
    private final Repository<Transaction> transactions;
    private final Repository<Goal> goals;
    private final Encoder encoder;

    public UserController(JwtProvider<JwtUser> provider,
                          Repository<User> users,
                          Repository<Account> accounts,
                          Repository<Budget> budgets,
                          Repository<Transaction> transactions,
                          Repository<Goal> goals,
                          Encoder encoder) {
        super(provider, users);
        this.accounts = accounts;
        this.budgets = budgets;
        this.transactions = transactions;
        this.goals = goals;
        this.encoder = encoder;
    }

    @Route(method = HandlerType.GET, route = "/")
    public void getSelf(Context ctx) {
        var user = getCheckedUser(ctx);
        if (user == null) {
            return;
        }
        ctx.json(user);
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = users.get(USER_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        var user = getUser(ctx);
        if (user == null || (user.getId() != found.getId() && !user.isAdmin())) {
            ctx.status(HttpStatus.FORBIDDEN);
            return;
        }
        ctx.json(found);
    }

    private boolean updateUser(Credentials creds, User user) {
        var login = creds.getLogin();
        var password = creds.getPassword();
        if (login == null && password == null) {
            return true;
        }
        if (login != null) {
            user.setLogin(login);
        }
        if (password != null) {
            user.setPassword(encoder.encode(password));
        }
        return false;
    }

    @Route(method = HandlerType.PATCH, route = "/")
    public void updateSelf(Context ctx) {
        var credentials = DtoUtil.parse(ctx, Credentials.class);
        if (credentials == null) {
            return;
        }
        var user = getCheckedUser(ctx);
        if (user == null) {
            return;
        }
        if (updateUser(credentials, user)) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        users.update(USER_ROLE, user);
        ctx.status(HttpStatus.OK);
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        var credentials = DtoUtil.parse(ctx, Credentials.class);
        if (credentials == null) {
            return;
        }
        var user = getCheckedUser(ctx);
        if (user == null) {
            return;
        }
        if (id != user.getId() && !user.isAdmin()) {
            ctx.status(HttpStatus.FORBIDDEN);
            return;
        }
        var toUpdate = id == user.getId() ? user : users.get(USER_ROLE, id);
        if (toUpdate == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        if (updateUser(credentials, toUpdate)) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        users.update(USER_ROLE, toUpdate);
        ctx.status(HttpStatus.OK);
    }

    private void list(Context ctx, Repository<?> repo) {
        var user = getCheckedUser(ctx);
        if (user == null) {
            return;
        }
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        if (id != user.getId() && !user.isAdmin()) {
            ctx.status(HttpStatus.FORBIDDEN);
            return;
        }
        if (!users.exists(USER_ROLE, id)) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        var found = repo.get(USER_ROLE, "owner", id);
        ctx.json(found);
    }

    @Route(method = HandlerType.GET, route = "/{id}/accounts")
    public void listAccounts(Context ctx) {
        list(ctx, accounts);
    }

    @Route(method = HandlerType.GET, route = "/{id}/budgets")
    public void listBudgets(Context ctx) {
        list(ctx, budgets);
    }

    @Route(method = HandlerType.GET, route = "/{id}/transactions")
    public void listTransactions(Context ctx) {
        list(ctx, transactions);
    }

    @Route(method = HandlerType.GET, route = "/{id}/goals")
    public void listGoals(Context ctx) {
        list(ctx, goals);
    }
}
