package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.*;
import com.github.romanqed.course.email.Mailer;
import com.github.romanqed.course.hash.Encoder;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

import java.util.*;
import java.util.concurrent.TimeUnit;

@JavalinController
public final class AuthController {
    private static final String SYSTEM_ROLE = "_service";

    private final Map<Integer, TwoFactorEntry> entries;
    private final Object lock;
    private final Repository<User> users;
    private final JwtProvider<JwtUser> jwt;
    private final Encoder encoder;
    private final Mailer mailer;

    public AuthController(Repository<User> users, JwtProvider<JwtUser> jwt, Encoder encoder, Mailer mailer) {
        this.users = users;
        this.jwt = jwt;
        this.encoder = encoder;
        this.mailer = mailer;
        this.entries = new HashMap<>();
        this.lock = new Object();
        if (mailer == null) {
            return;
        }
        var timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) {
                    for (var key : entries.keySet()) {
                        var entry = entries.get(key);
                        if (entry.isExpired()) {
                            entries.remove(key);
                        }
                    }
                }
            }
        }, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1));
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
        if (user.isTwoFactor()) {
            if (mailer == null) {
                ctx.result("Incorrect server configuration, contact with admin");
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                return;
            }
            synchronized (lock) {
                entries.computeIfAbsent(user.getId(), k -> {
                    var code = UUID.randomUUID().toString();
                    mailer.send(user.getEmail(), code);
                    return new TwoFactorEntry(code, 1);
                });
            }
            ctx.json(new Token());
        } else {
            ctx.json(new Token(makeToken(user)));
        }
    }

    @Route(method = HandlerType.POST, route = "/2fa")
    public void login2Fa(Context ctx) {
        var dto = DtoUtil.validate(ctx, TwoFactorDto.class);
        if (dto == null) {
            return;
        }
        var login = dto.getLogin();
        var user = users.getFirst(SYSTEM_ROLE, "login", login);
        if (user == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(new Response("Unknown user"));
            return;
        }
        synchronized (lock) {
            var id = user.getId();
            var found = entries.get(id);
            if (found == null) {
                ctx.status(HttpStatus.NOT_FOUND);
                ctx.json(new Response("2FA session dies"));
                return;
            }
            if (!found.getCode().equals(dto.getCode())) {
                ctx.status(HttpStatus.FORBIDDEN);
                ctx.json(new Response("Invalid 2FA code"));
                return;
            }
            entries.remove(id);
            ctx.json(new Token(makeToken(user)));
        }
    }
}
