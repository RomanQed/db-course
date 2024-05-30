package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.dto.ExchangeDto;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Exchange;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

import java.util.List;

@JavalinController("/exchange")
public final class ExchangeController extends AuthBase {
    private final Repository<Exchange> exchanges;

    public ExchangeController(JwtProvider<JwtUser> provider, Repository<User> users, Repository<Exchange> exchanges) {
        super(provider, users);
        this.exchanges = exchanges;
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
        var from = ctx.queryParamAsClass("from", Integer.class).getOrDefault(null);
        var to = ctx.queryParamAsClass("to", Integer.class).getOrDefault(null);
        if (from == null && to == null) {
            ctx.json(exchanges.get(USER_ROLE));
            return;
        }
        var fromFilter = "_from = " + from;
        var toFilter = "_to = " + to;
        if (from != null && to == null) {
            ctx.json(exchanges.get(USER_ROLE, fromFilter));
            return;
        }
        if (from == null) {
            ctx.json(exchanges.get(USER_ROLE, toFilter));
            return;
        }
        var common = fromFilter + " and " + toFilter;
        ctx.json(exchanges.get(USER_ROLE, common));
    }

    @Route(method = HandlerType.PUT)
    public void put(Context ctx) {
        var dto = DtoUtil.validate(ctx, ExchangeDto.class);
        if (dto == null) {
            return;
        }
        if (!checkAdmin(ctx)) {
            return;
        }
        var first = Exchange.of(dto.getFrom(), dto.getTo(), dto.getFactor());
        var second = Exchange.of(dto.getTo(), dto.getFrom(), 1 / dto.getFactor());
        exchanges.put(ADMIN_ROLE, first);
        exchanges.put(ADMIN_ROLE, second);
        ctx.json(List.of(first, second));
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.parse(ctx, ExchangeDto.class);
        if (dto == null) {
            return;
        }
        var factor = dto.getFactor();
        if (factor == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        if (!checkAdmin(ctx)) {
            return;
        }
        var found = exchanges.get(ADMIN_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        found.setFactor(factor);
        exchanges.update(ADMIN_ROLE, found);
        ctx.status(HttpStatus.OK);
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {
        Util.adminDelete(ctx, this, exchanges);
    }
}
