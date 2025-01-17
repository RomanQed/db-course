package com.github.romanqed.course;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class ContextWrapper {
    public final Context mock;
    public HttpStatus status;
    public Object body;

    ContextWrapper(Context mock) {
        this.mock = mock;
        this.status = HttpStatus.OK;
    }
}
