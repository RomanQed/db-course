package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.dto.UserUpdateDto;
import com.github.romanqed.course.hash.Encoder;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Account;
import com.github.romanqed.course.models.Budget;
import com.github.romanqed.course.models.Transaction;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

@JavalinController("/users")
public final class UserController extends AuthBase {
    private final Repository<Account> accounts;
    private final Repository<Budget> budgets;
    private final Repository<Transaction> transactions;
    private final Encoder encoder;
    private final Tracer tracer;

    public UserController(JwtProvider<JwtUser> provider,
                          Repository<User> users,
                          Repository<Account> accounts,
                          Repository<Budget> budgets,
                          Repository<Transaction> transactions,
                          Encoder encoder,
                          OpenTelemetry telemetry) {
        super(provider, users);
        this.accounts = accounts;
        this.budgets = budgets;
        this.transactions = transactions;
        this.encoder = encoder;
        this.tracer = telemetry.getTracer("com.github.romanqed.course.controllers.UserController");
    }

    private Span startSpan(String name, Context ctx) {
        return tracer.spanBuilder("UserController#" + name)
                .setAttribute("http.method", ctx.method().toString())
                .setAttribute("http.route", ctx.path())
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
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
        var span = startSpan("get", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = users.get(USER_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("UserNotFound");
            span.end();
            return;
        }
        var user = getUser(ctx);
        if (user == null || (user.getId() != found.getId() && !user.isAdmin())) {
            ctx.status(HttpStatus.FORBIDDEN);
            span.addEvent("UserCannotBeViewed");
            span.end();
            return;
        }
        ctx.json(found);
        span.addEvent("UserFound");
        span.end();
    }

    private boolean updateUser(UserUpdateDto dto, User user, Span span) {
        var password = dto.getPassword();
        var email = dto.getEmail();
        var twoFactor = dto.getTwoFactor();
        if (password == null && email == null) {
            span.addEvent("NothingToUpdate");
            return true;
        }
        if (email != null) {
            span.addEvent("EmailUpdated");
            user.setEmail(email);
        }
        if (twoFactor != null) {
            span.addEvent("2FAUpdated");
            if (twoFactor && user.getEmail() == null) {
                span.addEvent("MissingEmailFor2FA");
                return true;
            }
            user.setTwoFactor(twoFactor);
        }
        if (password != null) {
            span.addEvent("PasswordUpdated");
            user.setPassword(encoder.encode(password));
        }
        return false;
    }

    @Route(method = HandlerType.PATCH, route = "/")
    public void updateSelf(Context ctx) {
        var span = startSpan("updateSelf", ctx);
        var dto = DtoUtil.parse(ctx, UserUpdateDto.class, span);
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
        if (updateUser(dto, user, span)) {
            ctx.status(HttpStatus.BAD_REQUEST);
            span.end();
            return;
        }
        users.update(USER_ROLE, user);
        ctx.status(HttpStatus.OK);
        span.addEvent("UserUpdated");
        span.end();
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var span = startSpan("update", ctx);
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.parse(ctx, UserUpdateDto.class, span);
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
        if (id != user.getId() && !user.isAdmin()) {
            ctx.status(HttpStatus.FORBIDDEN);
            span.addEvent("UserCannotBeUpdated");
            span.end();
            return;
        }
        var toUpdate = id == user.getId() ? user : users.get(USER_ROLE, id);
        if (toUpdate == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("UserNotFound");
            span.end();
            return;
        }
        if (updateUser(dto, toUpdate, span)) {
            ctx.status(HttpStatus.BAD_REQUEST);
            span.end();
            return;
        }
        users.update(USER_ROLE, toUpdate);
        ctx.status(HttpStatus.OK);
        span.addEvent("UserUpdated");
        span.end();
    }

    private void list(Context ctx, Repository<?> repo, Span span) {
        var user = getCheckedUser(ctx);
        if (user == null) {
            span.addEvent("Unauthorized");
            span.end();
            return;
        }
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        if (id != user.getId() && !user.isAdmin()) {
            ctx.status(HttpStatus.FORBIDDEN);
            span.addEvent("DataCannotBeViewed");
            span.end();
            return;
        }
        if (!users.exists(USER_ROLE, id)) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("UserNotFound");
            span.end();
            return;
        }
        var found = repo.get(USER_ROLE, "owner", id);
        ctx.json(found);
        span.addEvent("DataListed");
    }

    @Route(method = HandlerType.GET, route = "/{id}/accounts")
    public void listAccounts(Context ctx) {
        var span = startSpan("listAccounts", ctx);
        list(ctx, accounts, span);
        span.end();
    }

    @Route(method = HandlerType.GET, route = "/{id}/budgets")
    public void listBudgets(Context ctx) {
        var span = startSpan("listBudgets", ctx);
        list(ctx, budgets, span);
        span.end();
    }

    @Route(method = HandlerType.GET, route = "/{id}/transactions")
    public void listTransactions(Context ctx) {
        var span = startSpan("listTransactions", ctx);
        list(ctx, transactions, span);
        span.end();
    }
}
