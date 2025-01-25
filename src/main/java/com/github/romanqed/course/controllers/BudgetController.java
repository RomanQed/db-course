package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.BudgetDto;
import com.github.romanqed.course.dto.BudgetStatus;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Budget;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@JavalinController("/budgets")
public final class BudgetController extends AuthBase {
    private final Connection connection;
    private final Repository<Budget> budgets;
    private final Repository<Currency> currencies;
    private final Tracer tracer;

    public BudgetController(JwtProvider<JwtUser> provider,
                            Connection connection,
                            Repository<User> users,
                            Repository<Budget> budgets,
                            Repository<Currency> currencies,
                            OpenTelemetry telemetry) {
        super(provider, users);
        this.connection = connection;
        this.budgets = budgets;
        this.currencies = currencies;
        this.tracer = telemetry.getTracer("com.github.romanqed.course.controllers.BudgetController");
    }

    private Span startSpan(String name, Context ctx) {
        return tracer.spanBuilder("BudgetController#" + name)
                .setAttribute("http.method", ctx.method().toString())
                .setAttribute("http.route", ctx.path())
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
    }

    private BudgetStatus get(int id, int user) throws SQLException {
        var sql = String.format("select get_budget_status(%d, %d)", id, user);
        var statement = connection.createStatement();
        var set = statement.executeQuery(sql);
        if (!set.next()) {
            throw new IllegalStateException("Cannot retrieve budget status");
        }
        var value = set.getObject(1, PGobject.class).getValue();
        if (value == null) {
            throw new IllegalStateException("Invalid postgresql response");
        }
        var raw = value.substring(1, value.length() - 1).split(",");
        var ret = new BudgetStatus();
        ret.setSpent(Double.parseDouble(raw[0]));
        ret.setGot(Double.parseDouble(raw[1]));
        ret.setTotal(Double.parseDouble(raw[2]));
        return ret;
    }

    @Route(method = HandlerType.GET, route = "/{id}/status")
    public void status(Context ctx) throws SQLException {
        var span = startSpan("status", ctx);
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = Util.seeOwned(ctx, this, budgets, id, span);
        if (found == null) {
            span.end();
            return;
        }
        var status = get(id, found.getOwner());
        ctx.json(status);
        span.addEvent("StatusCalculated");
        span.end();
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var span = startSpan("get", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = Util.seeOwned(ctx, this, budgets, id, span);
        if (found == null) {
            span.end();
            return;
        }
        ctx.json(found);
        span.addEvent("BudgetGot");
        span.end();
    }

    private Date nullifyTime(Date date) {
        var raw = Calendar.getInstance();
        raw.setTime(date);
        var ret = Calendar.getInstance();
        ret.setTimeInMillis(0);
        ret.set(Calendar.HOUR_OF_DAY, 0);
        ret.set(Calendar.YEAR, raw.get(Calendar.YEAR));
        ret.set(Calendar.MONTH, raw.get(Calendar.MONTH));
        ret.set(Calendar.DAY_OF_MONTH, raw.get(Calendar.DAY_OF_MONTH));
        return ret.getTime();
    }

    @Route(method = HandlerType.POST)
    public void post(Context ctx) {
        var span = startSpan("post", ctx);
        var dto = DtoUtil.validate(ctx, BudgetDto.class, span);
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
        var budget = Budget.of(user.getId(), currency, dto.getValue());
        budget.setStart(nullifyTime(dto.getStart()));
        budget.setEnd(nullifyTime(dto.getEnd()));
        budget.setDescription(Objects.requireNonNullElse(dto.getDescription(), ""));
        budgets.put(USER_ROLE, budget);
        ctx.json(budget);
        span.addEvent("BudgetAdded");
        span.end();
    }

    private void updateBudget(Context ctx, int id, Integer currency, String description, Double value, Span span) {
        var budget = Util.seeOwned(ctx, this, budgets, id, span);
        if (budget == null) {
            return;
        }
        if (currency != null) {
            span.addEvent("CurrencyUpdated");
            budget.setCurrency(currency);
        }
        if (description != null) {
            span.addEvent("DescriptionUpdated");
            budget.setDescription(description);
        }
        if (value != null) {
            span.addEvent("ValueUpdated");
            budget.setValue(value);
        }
        budgets.update(USER_ROLE, budget);
        span.addEvent("BudgetUpdated");
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var span = startSpan("update", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.parse(ctx, BudgetDto.class, span);
        if (dto == null) {
            span.end();
            return;
        }
        var currency = dto.getCurrency();
        var description = dto.getDescription();
        var value = dto.getValue();
        if (currency == null && description == null && value == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            span.addEvent("NothingToUpdate");
            span.end();
            return;
        }
        if (currency != null && !currencies.exists(USER_ROLE, currency)) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("MissingCurrency");
            span.end();
            return;
        }
        updateBudget(ctx, id, currency, description, value, span);
        span.end();
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {
        var span = startSpan("delete", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var budget = Util.seeOwned(ctx, this, budgets, id, span);
        if (budget == null) {
            span.end();
            return;
        }
        budgets.delete(USER_ROLE, budget.getId());
        span.addEvent("BudgetDeleted");
        span.end();
    }
}
