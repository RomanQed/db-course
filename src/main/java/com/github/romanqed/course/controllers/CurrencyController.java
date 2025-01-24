package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.dto.NameDto;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

@JavalinController("/currencies")
public final class CurrencyController extends AuthBase {
    private final Repository<Currency> currencies;
    private final Tracer tracer;

    public CurrencyController(JwtProvider<JwtUser> provider,
                              Repository<User> users,
                              Repository<Currency> currencies,
                              OpenTelemetry telemetry) {
        super(provider, users);
        this.currencies = currencies;
        this.tracer = telemetry.getTracer(
                "com.github.romanqed.course.controllers.CurrencyController"
        );
    }

    private Span startSpan(String name, Context ctx) {
        return tracer.spanBuilder("CurrencyController#" + name)
                .setAttribute("http.method", ctx.method().toString())
                .setAttribute("http.route", ctx.path())
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = currencies.get(USER_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        ctx.json(found);
    }

    @Route(method = HandlerType.GET, route = "/")
    public void find(Context ctx) {
        var span = startSpan("find", ctx);
        var name = ctx.queryParam("name");
        if (name == null) {
            ctx.json(currencies.get(USER_ROLE));
            span.addEvent("SelectAllCurrencies");
            span.end();
            return;
        }
        var found = currencies.get(USER_ROLE, "name like '%" + name + "%'");
        ctx.json(found);
        span.addEvent("SelectCurrenciesWithFilter", Attributes.builder()
                .put("name", name)
                .build()
        );
        span.end();
    }

    @Route(method = HandlerType.POST)
    public void post(Context ctx) {
        var span = startSpan("post", ctx);
        var dto = DtoUtil.validate(ctx, NameDto.class);
        if (dto == null) {
            span.addEvent("MissingName");
            span.end();
            return;
        }
        if (!checkAdmin(ctx, span)) {
            span.end();
            return;
        }
        var name = dto.getName();
        var category = Currency.of(name);
        currencies.put(ADMIN_ROLE, category);
        ctx.json(category);
        span.addEvent("CurrencyAdded", Attributes.builder()
                .put("name", name)
                .build()
        );
        span.end();
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var span = startSpan("update", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.validate(ctx, NameDto.class);
        if (dto == null) {
            span.addEvent("MissingName");
            span.end();
            return;
        }
        if (!checkAdmin(ctx, span)) {
            span.end();
            return;
        }
        var found = currencies.get(ADMIN_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("CurrencyNotFound");
            span.end();
            return;
        }
        found.setName(dto.getName());
        currencies.update(ADMIN_ROLE, found);
        span.addEvent("CurrencyUpdated");
        span.end();
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {
        var span = startSpan("delete", ctx);
        Util.adminDelete(ctx, this, currencies, span);
        span.end();
    }
}
