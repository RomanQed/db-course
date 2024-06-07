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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@JavalinController("/category")
public final class CategoryController extends AuthBase {
    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Repository<Category> categories;
    private final Repository<Transaction> transactions;

    public CategoryController(JwtProvider<JwtUser> provider,
                              Repository<User> users,
                              Repository<Category> categories,
                              Repository<Transaction> transactions) {
        super(provider, users);
        this.categories = categories;
        this.transactions = transactions;
    }

    private Range parseRange(Context ctx) {
        var rawFrom = ctx.queryParam("from");
        var rawTo = ctx.queryParam("to");
        var from = (Date) null;
        var to = (Date) null;
        try {
            if (rawFrom != null) {
                from = FORMATTER.parse(rawFrom);
            }
            if (rawTo != null) {
                to = FORMATTER.parse(rawTo);
            }
        } catch (ParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return null;
        }
        if (from != null && to != null && to.before(from)) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return null;
        }
        return new Range(from, to);
    }

    @Route(method = HandlerType.GET, route = "/{id}/transactions")
    public void listTransactions(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var user = getCheckedUser(ctx);
        if (user == null) {
            return;
        }
        var found = categories.get(USER_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        // Get date range from query params
        var range = parseRange(ctx);
        if (range == null) {
            return;
        }
        var where = "owner = " + user.getId() + " ";
        var from = range.getFrom() == null ? null : "'" + FORMATTER.format(range.getFrom()) + "'";
        var to = range.getTo() == null ? null : "'" + FORMATTER.format(range.getTo()) + "'";
        if (from != null && to != null) {
            where += "and (_timestamp between " + from + " and " + to + ")";
        } else if (from != null) {
            where += "and _timestamp > " + from;
        } else if (to != null) {
            where += "and _timestamp < " + to;
        }
        var result = transactions.get(USER_ROLE, where);
        ctx.json(result);
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
        var name = ctx.queryParam("name");
        if (name == null) {
            ctx.json(categories.get(USER_ROLE));
            return;
        }
        var found = categories.get(USER_ROLE, "name like '" + name + "'");
        ctx.json(found);
    }

    @Route(method = HandlerType.PUT)
    public void put(Context ctx) {
        var dto = DtoUtil.validate(ctx, NameDto.class);
        if (dto == null) {
            return;
        }
        if (!checkAdmin(ctx)) {
            return;
        }
        var category = Category.of(dto.getName());
        categories.put(ADMIN_ROLE, category);
        ctx.json(category);
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.validate(ctx, NameDto.class);
        if (dto == null) {
            return;
        }
        if (!checkAdmin(ctx)) {
            return;
        }
        var found = categories.get(ADMIN_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        found.setName(dto.getName());
        categories.update(ADMIN_ROLE, found);
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {
        Util.adminDelete(ctx, this, categories);
    }
}
