package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.AccountDto;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Account;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

import java.util.Objects;

@JavalinController("/accounts")
public final class AccountController extends AuthBase {
    private final Repository<Account> accounts;
    private final Repository<Currency> currencies;
    private final Tracer tracer;

    public AccountController(JwtProvider<JwtUser> provider,
                             Repository<User> users,
                             Repository<Account> accounts,
                             Repository<Currency> currencies,
                             OpenTelemetry telemetry) {
        super(provider, users);
        this.accounts = accounts;
        this.currencies = currencies;
        this.tracer = telemetry.getTracer("com.github.romanqed.course.controllers.AccountController");
    }

    private Span startSpan(String name, Context ctx) {
        return tracer.spanBuilder("AccountController#" + name)
                .setAttribute("http.method", ctx.method().toString())
                .setAttribute("http.route", ctx.path())
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var span = startSpan("get", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = Util.seeOwned(ctx, this, accounts, id, span);
        if (found == null) {
            span.end();
            return;
        }
        ctx.json(found);
        span.end();
    }

    @Route(method = HandlerType.POST)
    public void post(Context ctx) {
        var span = startSpan("post", ctx);
        var dto = DtoUtil.validate(ctx, AccountDto.class, span);
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
        var currency = dto.getCurrency();
        if (!currencies.exists(USER_ROLE, currency)) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("MissingCurrency");
            span.end();
            return;
        }
        var account = Account.of(user.getId(), currency, dto.getValue());
        account.setDescription(Objects.requireNonNullElse(dto.getDescription(), ""));
        accounts.put(USER_ROLE, account);
        ctx.json(account);
        span.addEvent("AccountAdded");
        span.end();
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var span = startSpan("update", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.parse(ctx, AccountDto.class, span);
        if (dto == null) {
            span.end();
            return;
        }
        var currency = dto.getCurrency();
        var description = dto.getDescription();
        if (currency == null && description == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            span.addEvent("NothingToUpdate");
            span.end();
            return;
        }
        var account = Util.seeOwned(ctx, this, accounts, id, span);
        if (account == null) {
            span.end();
            return;
        }
        if (currency != null) {
            if (!currencies.exists(USER_ROLE, currency)) {
                ctx.status(HttpStatus.NOT_FOUND);
                span.addEvent("MissingCurrency");
                span.end();
                return;
            }
            account.setCurrency(currency);
        }
        if (description != null) {
            account.setDescription(description);
        }
        accounts.update(USER_ROLE, account);
        span.addEvent("AccountUpdated");
        span.end();
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {
        var span = startSpan("delete", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var account = Util.seeOwned(ctx, this, accounts, id, span);
        if (account == null) {
            span.end();
            return;
        }
        accounts.delete(USER_ROLE, account.getId());
        span.addEvent("AccountDeleted");
        span.end();
    }
}
