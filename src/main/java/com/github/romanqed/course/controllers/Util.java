package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.models.Owned;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import static com.github.romanqed.course.controllers.AuthBase.ADMIN_ROLE;
import static com.github.romanqed.course.controllers.AuthBase.USER_ROLE;

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

    static <T extends Owned> T seeOwned(Context ctx, AuthBase auth, Repository<T> repository, int id) {
        var user = auth.getCheckedUser(ctx);
        if (user == null) {
            return null;
        }
        var found = repository.get(USER_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            return null;
        }
        if (user.getId() != found.getOwner() && !user.isAdmin()) {
            ctx.status(HttpStatus.FORBIDDEN);
            return null;
        }
        return found;
    }
}
