package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Exchange;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;

@JavalinController("/exchange")
public final class ExchangeController extends AuthBase {
    private final Repository<Exchange> exchanges;

    public ExchangeController(JwtProvider<JwtUser> provider, Repository<User> users, Repository<Exchange> exchanges) {
        super(provider, users);
        this.exchanges = exchanges;
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {

    }

    @Route(method = HandlerType.GET)
    public void find(Context ctx) {

    }

    @Route(method = HandlerType.PUT)
    public void put(Context ctx) {

    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {

    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {

    }
}
