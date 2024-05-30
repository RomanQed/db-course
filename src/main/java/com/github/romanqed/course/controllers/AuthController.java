package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.Credentials;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.dto.Response;
import com.github.romanqed.course.dto.Token;
import com.github.romanqed.course.hash.Encoder;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

@JavalinController
public final class AuthController {
    private static final String SYSTEM_ROLE = "_service";

    private final Repository<User> users;
    private final JwtProvider<JwtUser> jwt;
    private final Encoder encoder;

    public AuthController(Repository<User> users, JwtProvider<JwtUser> jwt, Encoder encoder) {
        this.users = users;
        this.jwt = jwt;
        this.encoder = encoder;
    }

    private String makeToken(User user) {
        var jwtUser = new JwtUser(user.getId(), user.getLogin(), user.isAdmin());
        return jwt.generateToken(jwtUser);
    }

    @Route(method = HandlerType.POST, route = "/register")
    public void register(Context ctx) {
        var credentials = DtoUtil.validate(ctx, Credentials.class);
        if (credentials == null) {
            return;
        }
        var login = credentials.getLogin();
        if (users.exists(SYSTEM_ROLE, "login", login)) {
            ctx.status(HttpStatus.CONFLICT);
            ctx.json(new Response("User with the specified login already exists"));
            return;
        }
        var hashed = encoder.encode(credentials.getPassword());
        var user = User.of(login, hashed);
        users.put(SYSTEM_ROLE, user);
        var token = makeToken(user);
        ctx.json(new Token(token));
    }

    @Route(method = HandlerType.POST, route = "/login")
    public void login(Context ctx) {
        var credentials = DtoUtil.validate(ctx, Credentials.class);
        if (credentials == null) {
            return;
        }
        var login = credentials.getLogin();
        var user = users.getFirst(SYSTEM_ROLE, "login", login);
        if (user == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(new Response("Unknown user"));
            return;
        }
        if (!encoder.matches(credentials.getPassword(), user.getPassword())) {
            ctx.status(HttpStatus.UNAUTHORIZED);
            ctx.json(new Response("Invalid credentials"));
            return;
        }
        ctx.json(new Token(makeToken(user)));
    }
}
