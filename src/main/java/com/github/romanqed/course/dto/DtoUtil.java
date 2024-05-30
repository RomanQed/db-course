package com.github.romanqed.course.dto;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class DtoUtil {
    private DtoUtil() {
    }

    private static int parse(String raw, int def) {
        if (raw == null) {
            return def;
        }
        var ret = Integer.parseInt(raw);
        if (ret < 1) {
            throw new IllegalArgumentException("Invalid value");
        }
        return ret;
    }

    public static <T> T parse(Context ctx, Class<T> clazz) {
        try {
            var ret = ctx.bodyAsClass(clazz);
            if (ret == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(new Response("Missing body"));
            }
            return ret;
        } catch (Throwable e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new Response("Bad request", e));
            return null;
        }
    }

    public static <T extends Validated> T validate(Context ctx, Class<T> clazz) {
        try {
            var ret = ctx.bodyAsClass(clazz);
            if (ret == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(new Response("Missing body"));
                return null;
            }
            ret.validate();
            return ret;
        } catch (ValidateException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new Response(e.getMessage()));
            return null;
        } catch (Throwable e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new Response("Bad request", e));
            return null;
        }
    }
}
