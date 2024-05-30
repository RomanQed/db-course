package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import static com.github.romanqed.course.controllers.AuthBase.ADMIN_ROLE;

final class Util {
    private Util() {
    }

    static void adminDelete(Context ctx, AuthBase base, Repository<?> repository) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        if (!base.checkAdmin(ctx)) {
            return;
        }
        if (!repository.exists(ADMIN_ROLE, id)) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        repository.delete(ADMIN_ROLE, id);
    }
}
