package com.github.romanqed.course;

import io.javalin.http.Context;
import org.mockito.Mockito;

final class CtxMockUtil {
    private CtxMockUtil() {
    }

    static ContextWrapper mockContext() {
        var mock = Mockito.mock(Context.class);
        var ret = new ContextWrapper(mock);
        Mockito.doAnswer(inv -> {
            ret.status = inv.getArgument(0);
            return inv.getMock();
        }).when(mock).status(Mockito.any());
        Mockito.doAnswer(inv -> {
            ret.body = inv.getArgument(0);
            return inv.getMock();
        }).when(mock).json(Mockito.any());
        return ret;
    }

    static ContextBuilder builder() {
        return new ContextBuilder(CtxMockUtil::mockContext);
    }
}
