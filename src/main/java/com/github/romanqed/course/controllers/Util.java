package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.models.Owned;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.trace.Span;

import static com.github.romanqed.course.controllers.AuthBase.ADMIN_ROLE;
import static com.github.romanqed.course.controllers.AuthBase.USER_ROLE;

final class Util {
    private Util() {
    }

    static void adminDelete(Context ctx, AuthBase base, Repository<?> repository, Span span) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        if (!base.checkAdmin(ctx, span)) {
            return;
        }
        if (!repository.exists(ADMIN_ROLE, id)) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("NotFound");
            return;
        }
        repository.delete(ADMIN_ROLE, id);
        span.addEvent("Deleted");
    }

    static <T extends Owned> T seeOwned(Context ctx, AuthBase auth, Repository<T> repository, int id, Span span) {
        var user = auth.getCheckedUser(ctx);
        if (user == null) {
            span.addEvent("Unauthorized");
            return null;
        }
        var found = repository.get(USER_ROLE, id);
        if (found == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            span.addEvent("NotFound");
            return null;
        }
        if (user.getId() != found.getOwner() && !user.isAdmin()) {
            ctx.status(HttpStatus.FORBIDDEN);
            span.addEvent("NotOwnedByUser");
            return null;
        }
        span.addEvent("SeeOwned");
        return found;
    }
}
