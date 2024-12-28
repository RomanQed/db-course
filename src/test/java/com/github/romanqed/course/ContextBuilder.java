package com.github.romanqed.course;

import io.javalin.validation.Params;
import io.javalin.validation.Validator;
import org.mockito.Mockito;

import java.util.Map;
import java.util.function.Supplier;

final class ContextBuilder {
    final Supplier<ContextWrapper> supplier;
    ContextWrapper wrapper;

    ContextBuilder(Supplier<ContextWrapper> supplier) {
        this.wrapper = null;
        this.supplier = supplier;
    }

    void check() {
        if (wrapper == null) {
            wrapper = supplier.get();
        }
    }

    ContextBuilder withBody(Object body) {
        check();
        Mockito.when(wrapper.mock.bodyAsClass((Class) body.getClass())).thenReturn(body);
        return this;
    }

    ContextBuilder withAuth() {
        check();
        Mockito.when(wrapper.mock.header("Authorization")).thenReturn("Bearer mock");
        return this;
    }

    private static Validator createValidator(Object val, Class<?> cl) {
        return new Validator<>(new Params<>("", (Class<Object>) cl, "", val, () -> null));
    }

    ContextBuilder withPath(String s, Object v) {
        check();
        if (v.getClass() == String.class) {
            Mockito.when(wrapper.mock.pathParam(s)).thenReturn((String) v);
        } else {
            Mockito
                    .when(wrapper.mock.pathParamAsClass(s, v.getClass()))
                    .thenReturn(createValidator(v, v.getClass()));
        }
        return this;
    }

    ContextBuilder withPaths(Map<String, Object> paths) {
        check();
        for (var entry : paths.entrySet()) {
            var value = entry.getValue();
            if (value.getClass() == String.class) {
                Mockito.when(wrapper.mock.pathParam(entry.getKey())).thenReturn((String) value);
            } else {
                Mockito
                        .when(wrapper.mock.pathParamAsClass(entry.getKey(), value.getClass()))
                        .thenReturn(createValidator(value, value.getClass()));
            }
        }
        return this;
    }

    ContextBuilder withQuery(String s, Object v) {
        check();
        if (v.getClass() == String.class) {
            Mockito.when(wrapper.mock.queryParam(s)).thenReturn((String) v);
        } else {
            Mockito
                    .when(wrapper.mock.queryParamAsClass(s, v.getClass()))
                    .thenReturn(createValidator(v, v.getClass()));
        }
        return this;
    }

    ContextBuilder withQueries(Map<String, Object> queries) {
        check();
        for (var entry : queries.entrySet()) {
            var value = entry.getValue();
            if (value.getClass() == String.class) {
                Mockito.when(wrapper.mock.queryParam(entry.getKey())).thenReturn((String) value);
            } else {
                Mockito
                        .when(wrapper.mock.queryParamAsClass(entry.getKey(), value.getClass()))
                        .thenReturn(createValidator(value, value.getClass()));
            }
        }
        return this;
    }

    ContextWrapper build() {
        if (wrapper == null) {
            return supplier.get();
        }
        var ret = wrapper;
        wrapper = null;
        return ret;
    }
}
