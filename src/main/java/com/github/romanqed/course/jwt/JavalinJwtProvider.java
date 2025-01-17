package com.github.romanqed.course.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import javalinjwt.JWTProvider;

import java.util.Optional;

public final class JavalinJwtProvider<T> implements JwtProvider<T> {
    private final JWTProvider<T> provider;

    public JavalinJwtProvider(JWTProvider<T> provider) {
        this.provider = provider;
    }

    @Override
    public String generateToken(T obj) {
        return provider.generateToken(obj);
    }

    @Override
    public Optional<DecodedJWT> validateToken(String token) {
        return provider.validateToken(token);
    }
}
