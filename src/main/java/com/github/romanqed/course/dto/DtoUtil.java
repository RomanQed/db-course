package com.github.romanqed.course.dto;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;

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

    public static <T> T parse(Context ctx, Class<T> clazz, Span span) {
        try {
            var ret = ctx.bodyAsClass(clazz);
            if (ret == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(new Response("Missing body"));
                span.addEvent("MissingBody");
            }
            span.addEvent("BodyParsed");
            return ret;
        } catch (Throwable e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new Response("Bad request", e));
            span.addEvent("ParseError", Attributes.builder()
                    .put("exception", e.toString())
                    .build()
            );
            return null;
        }
    }

    public static <T extends Validated> T validate(Context ctx, Class<T> clazz, Span span) {
        try {
            var ret = ctx.bodyAsClass(clazz);
            if (ret == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(new Response("Missing body"));
                span.addEvent("MissingBody");
                return null;
            }
            ret.validate();
            span.addEvent("BodyValidated");
            return ret;
        } catch (ValidateException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            var message = e.getMessage();
            ctx.json(new Response(message));
            span.addEvent("ValidateError", Attributes.builder()
                    .put("error", message)
                    .build()
            );
            return null;
        } catch (Throwable e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new Response("Bad request", e));
            span.addEvent("ValidateError", Attributes.builder()
                    .put("exception", e.toString())
                    .build()
            );
            return null;
        }
    }
}
