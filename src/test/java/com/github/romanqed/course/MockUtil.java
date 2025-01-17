package com.github.romanqed.course;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import io.javalin.http.Context;
import org.mockito.Mockito;

import java.util.Optional;

public final class MockUtil {
    private MockUtil() {
    }

    public static JwtProvider<JwtUser> mockProvider(int userId) {
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

    public static ContextWrapper mockCtx() {
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

    public static ContextBuilder ctxBuilder() {
        return new ContextBuilder(MockUtil::mockCtx);
    }
}
