package com.github.romanqed.course;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import io.javalin.http.Context;
import io.javalin.validation.Params;
import io.javalin.validation.Validator;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Optional;

final class MockUtil {
    private MockUtil() {
    }

    static Validator createValidator(Object val, Class<?> cl) {
        return new Validator<>(new Params<>("", (Class<Object>) cl, "", val, () -> null));
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

    static ContextWrapper mockContext(boolean auth, Map<String, Object> paths, Map<String, Object> queries, Object body) {
        var ret = mockContext();
        var mock = ret.mock;
        if (auth) {
            Mockito.when(mock.header("Authorization")).thenReturn("Bearer mock");
        }
        for (var entry : paths.entrySet()) {
            var value = entry.getValue();
            if (value.getClass() == String.class) {
                Mockito.when(mock.pathParam(entry.getKey())).thenReturn((String) value);
            } else {
                Mockito
                        .when(mock.pathParamAsClass(entry.getKey(), value.getClass()))
                        .thenReturn(createValidator(value, value.getClass()));
            }
        }
        for (var entry : queries.entrySet()) {
            var value = entry.getValue();
            if (value.getClass() == String.class) {
                Mockito.when(mock.queryParam(entry.getKey())).thenReturn((String) value);
            } else {
                Mockito
                        .when(mock.queryParamAsClass(entry.getKey(), value.getClass()))
                        .thenReturn(createValidator(value, value.getClass()));
            }
        }
        if (body != null) {
            Mockito.when(mock.bodyAsClass((Class) body.getClass())).thenReturn(body);
        }
        return ret;
    }

    static JwtProvider<JwtUser> mockProvider(int userId) {
        var id = Mockito.mock(Claim.class);
        Mockito.when(id.asInt()).thenReturn(userId);
        var decoded = Mockito.mock(DecodedJWT.class);
        Mockito.when(decoded.getClaim("id")).thenReturn(id);
        return new JwtProvider<>() {

            @Override
            public String generateToken(JwtUser obj) {
                return obj.getId() + ":" + obj.getLogin() + ":" + obj.isAdmin();
            }

            @Override
            public Optional<DecodedJWT> validateToken(String token) {
                return Optional.of(decoded);
            }
        };
    }
}
