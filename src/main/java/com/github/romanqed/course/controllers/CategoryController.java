package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.dto.NameDto;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@JavalinController("/categories")
public final class CategoryController extends AuthBase {
    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat INPUT = new SimpleDateFormat("yyyy-MM-dd");
    private final Repository<Category> categories;
    private final Repository<Transaction> transactions;
    private final Tracer tracer;

    public CategoryController(JwtProvider<JwtUser> provider,
                              Repository<User> users,
                              Repository<Category> categories,
                              Repository<Transaction> transactions,
                              OpenTelemetry telemetry) {
        super(provider, users);
        this.categories = categories;
        this.transactions = transactions;
        this.tracer = telemetry.getTracer(
                "com.github.romanqed.course.controllers.CategoryController"
        );
    }

    private Span startSpan(String name, Context ctx) {
        return tracer.spanBuilder("CategoryController#" + name)
                .setAttribute("http.method", ctx.method().toString())
                .setAttribute("http.route", ctx.path())
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
    }

    private Range parseRange(Context ctx, Span span) {
        var rawFrom = ctx.queryParam("from");
        var rawTo = ctx.queryParam("to");
        var from = (Date) null;
        var to = (Date) null;
        try {
            if (rawFrom != null) {
                span.addEvent("FromParse");
                from = INPUT.parse(rawFrom);
            }
            if (rawTo != null) {
                span.addEvent("ToParse");
                to = INPUT.parse(rawTo);
            }
        } catch (ParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            span.addEvent("ParseFailed");
            return null;
        }
        if (from != null && to != null && to.before(from)) {
            ctx.status(HttpStatus.BAD_REQUEST);
            span.addEvent("InvalidRange", Attributes.builder()
                    .put("from", from.toString())
                    .put("to", to.toString())
                    .build()
            );
            return null;
        }
        return new Range(from, to);
    }

    @Route(method = HandlerType.GET, route = "/{id}/transactions")
    public void listTransactions(Context ctx) {
        var span = startSpan("listTransactions", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var user = getCheckedUser(ctx);
        if (user == null) {
            span.addEvent("Unauthorized");
            span.end();
            return;
        }
        span.addEvent("GotUser", Attributes.builder()
                .put("login", user.getLogin())
                .build()
        );
        var found = categories.get(USER_ROLE, id);
        if (found == null) {
            span.addEvent("CategoryNotFound");
            span.end();
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        // Get date range from query params
        var range = parseRange(ctx, span);
        if (range == null) {
            span.end();
            return;
        }
        var where = "category = " + id + " and owner = " + user.getId() + " ";
        var from = range.getFrom() == null ? null : "'" + FORMATTER.format(range.getFrom()) + "'";
        var to = range.getTo() == null ? null : "'" + FORMATTER.format(range.getTo()) + "'";
        if (from != null && to != null) {
            where += "and (_timestamp between " + from + " and " + to + ")";
        } else if (from != null) {
            where += "and _timestamp > " + from;
        } else if (to != null) {
            where += "and _timestamp < " + to;
        }
        span.addEvent("QueryPrepared", Attributes.builder()
                .put("query", where)
                .build()
        );
        var result = transactions.get(USER_ROLE, where);
        ctx.json(result);
        span.addEvent("TransactionsListed");
        span.end();
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = categories.get(USER_ROLE, id);
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
            ctx.json(categories.get(USER_ROLE));
            span.addEvent("SelectAllCategories");
            span.end();
            return;
        }
        var found = categories.get(USER_ROLE, "name like '%" + name + "%'");
        ctx.json(found);
        span.addEvent("SelectCategoriesWithFilter", Attributes.builder()
                .put("name", name)
                .build()
        );
        span.end();
    }

    @Route(method = HandlerType.POST)
    public void post(Context ctx) {
        var span = startSpan("post", ctx);
        var dto = DtoUtil.validate(ctx, NameDto.class, span);
        if (dto == null) {
            span.end();
            return;
        }
        if (!checkAdmin(ctx, span)) {
            span.end();
            return;
        }
        var category = Category.of(dto.getName());
        categories.put(ADMIN_ROLE, category);
        ctx.json(category);
        span.addEvent("CategoryCreated");
        span.end();
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var span = startSpan("update", ctx);
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.validate(ctx, NameDto.class, span);
        if (dto == null) {
            span.end();
            return;
        }
        if (!checkAdmin(ctx, span)) {
            span.end();
            return;
        }
        var found = categories.get(ADMIN_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("CategoryNotFound");
            span.end();
            return;
        }
        found.setName(dto.getName());
        categories.update(ADMIN_ROLE, found);
        span.addEvent("CategoryUpdated");
        span.end();
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {
        var span = startSpan("delete", ctx);
        Util.adminDelete(ctx, this, categories, span);
        span.end();
    }
}
