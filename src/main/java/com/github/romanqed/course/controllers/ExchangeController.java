package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.dto.ExchangeDto;
import com.github.romanqed.course.dto.Response;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.Exchange;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

import java.util.List;

@JavalinController("/exchanges")
public final class ExchangeController extends AuthBase {
    private final Repository<Exchange> exchanges;
    private final Repository<Currency> currencies;
    private final Tracer tracer;

    public ExchangeController(JwtProvider<JwtUser> provider,
                              Repository<User> users,
                              Repository<Exchange> exchanges,
                              Repository<Currency> currencies,
                              OpenTelemetry telemetry) {
        super(provider, users);
        this.exchanges = exchanges;
        this.currencies = currencies;
        this.tracer = telemetry.getTracer(
                "com.github.romanqed.course.controllers.ExchangeController"
        );
    }

    private Span startSpan(String name, Context ctx) {
        return tracer.spanBuilder("ExchangeController#" + name)
                .setAttribute("http.method", ctx.method().toString())
                .setAttribute("http.route", ctx.path())
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = exchanges.get(USER_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        ctx.json(found);
    }

    @Route(method = HandlerType.GET, route = "/")
    public void find(Context ctx) {
        var span = startSpan("find", ctx);
        var from = ctx.queryParamAsClass("from", Integer.class).getOrDefault(null);
        var to = ctx.queryParamAsClass("to", Integer.class).getOrDefault(null);
        if (from == null && to == null) {
            span.addEvent("SelectAllExchanges");
            ctx.json(exchanges.get(USER_ROLE));
            span.end();
            return;
        }
        var fromFilter = "_from = " + from;
        var toFilter = "_to = " + to;
        if (from != null && to == null) {
            span.addEvent("SelectFromExchanges", Attributes.builder()
                    .put("from", fromFilter)
                    .build()
            );
            ctx.json(exchanges.get(USER_ROLE, fromFilter));
            span.end();
            return;
        }
        if (from == null) {
            span.addEvent("SelectToExchanges", Attributes.builder()
                    .put("to", toFilter)
                    .build()
            );
            ctx.json(exchanges.get(USER_ROLE, toFilter));
            span.end();
            return;
        }
        var common = fromFilter + " and " + toFilter;
        span.addEvent("SelectExchanges", Attributes.builder()
                .put("filter", common)
                .build()
        );
        ctx.json(exchanges.get(USER_ROLE, common));
        span.end();
    }

    @Route(method = HandlerType.POST)
    public void post(Context ctx) {
        var span = startSpan("post", ctx);
        var dto = DtoUtil.validate(ctx, ExchangeDto.class, span);
        if (dto == null) {
            return;
        }
        if (!checkAdmin(ctx, span)) {
            return;
        }
        var from = dto.getFrom();
        var to = dto.getTo();
        if (!currencies.exists(ADMIN_ROLE, from) || !currencies.exists(ADMIN_ROLE, to)) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("MissingCurrencies");
            span.end();
            return;
        }
        if (exchanges.exists(ADMIN_ROLE, "_from = " + from + " and _to = " + to)) {
            ctx.status(HttpStatus.CONFLICT);
            span.addEvent("FromToExchangeDuplicate");
            span.end();
            return;
        }
        if (exchanges.exists(ADMIN_ROLE, "_from = " + to + " and _to = " + from)) {
            ctx.status(HttpStatus.CONFLICT);
            span.addEvent("ToFromExchangeDuplicate");
            span.end();
            return;
        }
        var first = Exchange.of(from, to, dto.getFactor());
        var second = Exchange.of(to, from, 1 / dto.getFactor());
        exchanges.put(ADMIN_ROLE, first);
        exchanges.put(ADMIN_ROLE, second);
        ctx.json(List.of(first, second));
        span.addEvent("ExchangeAdded");
        span.end();
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var span = startSpan("update", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.parse(ctx, ExchangeDto.class, span);
        if (dto == null) {
            span.end();
            return;
        }
        var factor = dto.getFactor();
        if (factor == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            span.addEvent("MissingFactor");
            span.end();
            return;
        }
        if (factor <= 0) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new Response("Invalid factor"));
            span.addEvent("FactorLessOrEqualThanZero");
            span.end();
            return;
        }
        if (!checkAdmin(ctx, span)) {
            span.end();
            return;
        }
        var found = exchanges.get(ADMIN_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("ExchangeNotFound");
            span.end();
            return;
        }
        found.setFactor(factor);
        exchanges.update(ADMIN_ROLE, found);
        var second = exchanges.get(ADMIN_ROLE, "_from = " + found.getTo() + " and _to = " + found.getFrom());
        if (!second.isEmpty()) {
            var e = second.get(0);
            e.setFactor(1 / factor);
            exchanges.update(ADMIN_ROLE, e);
        }
        ctx.status(HttpStatus.OK);
        span.addEvent("ExchangeUpdated");
        span.end();
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {
        var span = startSpan("delete", ctx);
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        var first = exchanges.get(ADMIN_ROLE, id);
        if (first == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("ExchangeNotFound");
            span.end();
            return;
        }
        if (!checkAdmin(ctx, span)) {
            span.end();
            return;
        }
        exchanges.delete(ADMIN_ROLE, id);
        span.addEvent("FirstExchangeDeleted");
        var second = exchanges.get(ADMIN_ROLE, "_from = " + first.getTo() + " and _to = " + first.getFrom());
        if (!second.isEmpty()) {
            exchanges.delete(ADMIN_ROLE, second.get(0).getId());
            span.addEvent("SecondExchangeDeleted");
        }
        ctx.status(HttpStatus.OK);
        span.end();
    }
}
