package com.github.romanqed.course;

import com.github.romanqed.course.controllers.AuthController;
import com.github.romanqed.course.dto.Credentials;
import com.github.romanqed.course.dto.Token;
import com.github.romanqed.course.models.User;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class AuthControllerTest {

    @Test
    public void testRegister() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            String key;
            Object value;
            Object model;

            @Override
            public boolean exists(String role, String field, Object value) {
                this.key = field;
                this.value = value;
                return false;
            }

            @Override
            public void put(String role, User model) {
                this.model = model;
            }
        };
        var ct = new AuthController(users, jwt, new EncoderImpl());
        var creds = new Credentials();
        creds.setLogin("user");
        creds.setPassword("pswd");
        var ctx = CtxMockUtil.builder()
                .withBody(creds)
                .build();

        ct.register(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        var token = (Token) ctx.body;
        assertEquals("0:user:false", token.getToken());
        assertEquals("login", users.key);
        assertEquals("user", users.value);
        var model = (User) users.model;
        assertEquals(0, model.getId());
        assertEquals("user", model.getLogin());
        assertEquals("pswd", model.getPassword());
        assertFalse(model.isAdmin());
    }

    @Test
    public void testRegisterNoBody() {
        var ct = new AuthController(null, null, null);
        var ctx = CtxMockUtil.mockContext();

        ct.register(ctx.mock);

        assertEquals(HttpStatus.BAD_REQUEST, ctx.status);
    }

    @Test
    public void testLogin() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            String login;

            @Override
            public User getFirst(String role, String field, Object value) {
                login = (String) value;
                return User.of((String) value, "pass");
            }
        };
        var ct = new AuthController(users, jwt, new EncoderImpl());
        var creds = new Credentials();
        creds.setLogin("log");
        creds.setPassword("pass");
        var ctx = CtxMockUtil.builder()
                .withBody(creds)
                .build();

        ct.login(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        var token = (Token) ctx.body;
        assertEquals("0:log:false", token.getToken());
        assertEquals("log", users.login);
    }

    @Test
    public void testLoginNoBody() {
        var ct = new AuthController(null, null, null);
        var ctx = CtxMockUtil.mockContext();

        ct.login(ctx.mock);

        assertEquals(HttpStatus.BAD_REQUEST, ctx.status);
    }
}
