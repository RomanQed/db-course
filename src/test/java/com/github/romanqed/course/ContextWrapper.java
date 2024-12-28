package com.github.romanqed.course;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

final class ContextWrapper {
    final Context mock;
    HttpStatus status;
    Object body;

    ContextWrapper(Context mock) {
        this.mock = mock;
        this.status = HttpStatus.OK;
    }
}
