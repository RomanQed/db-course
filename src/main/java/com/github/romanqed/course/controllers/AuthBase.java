package com.github.romanqed.course.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.Response;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import javalinjwt.JavalinJWT;
import kotlin.jvm.functions.Function2;

public class AuthBase {
    protected static final String SYSTEM_ROLE = "_service";
    protected static final String USER_ROLE = "_user";
    protected static final String ADMIN_ROLE = "_admin";

    protected final JwtProvider<JwtUser> provider;
    protected final Repository<User> users;

    protected AuthBase(JwtProvider<JwtUser> provider, Repository<User> users) {
        this.provider = provider;
        this.users = users;
    }

    protected DecodedJWT auth(Context ctx) {
        var decoded = JavalinJWT.getTokenFromHeader(ctx).flatMap(provider::validateToken);
        if (decoded.isEmpty()) {
            ctx.status(HttpStatus.UNAUTHORIZED);
            ctx.json(new Response("Missing or invalid token"));
            return null;
        }
        return decoded.get();
    }

    protected Integer getCheckedUserId(Context ctx) {
        var decoded = auth(ctx);
        if (decoded == null) {
            return null;
        }
        return decoded.getClaim("id").asInt();
    }

    protected User getUser(Context ctx, Function2<Repository<User>, Integer, User> getter) {
        var decoded = JavalinJWT.getTokenFromHeader(ctx).flatMap(provider::validateToken);
        return decoded
                .map(decodedJWT -> getter.invoke(users, decodedJWT.getClaim("id").asInt()))
                .orElse(null);
    }

    protected User getUser(Context ctx) {
        return getUser(ctx, (r, id) -> r.get(USER_ROLE, id));
    }

    protected User getCheckedUser(Context ctx, Function2<Repository<User>, Integer, User> getter) {
        var id = getCheckedUserId(ctx);
        if (id == null) {
            return null;
        }
        var ret = getter.invoke(users, id);
        if (ret == null) {
            ctx.status(HttpStatus.UNAUTHORIZED);
            ctx.json(new Response("User not found"));
            return null;
        }
        return ret;
    }

    protected User getCheckedUser(Context ctx) {
        return getCheckedUser(ctx, (r, id) -> r.get(USER_ROLE, id));
    }

    protected boolean checkAdmin(Context ctx) {
        var user = getCheckedUser(ctx);
        if (user == null) {
            return false;
        }
        if (!user.isAdmin()) {
            ctx.status(HttpStatus.FORBIDDEN);
            return false;
        }
        return true;
    }
}
