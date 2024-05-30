package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;

@JavalinController
public final class CurrencyController extends AuthBase {
    private final Repository<Currency> currencies;

    public CurrencyController(JwtProvider<JwtUser> provider, Repository<User> users, Repository<Currency> currencies) {
        super(provider, users);
        this.currencies = currencies;
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
