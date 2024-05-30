package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Account;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;

@JavalinController("/account")
public final class AccountController extends AuthBase {
    private final Repository<Account> accounts;

    public AccountController(JwtProvider<JwtUser> provider, Repository<User> users, Repository<Account> accounts) {
        super(provider, users);
        this.accounts = accounts;
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {

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
