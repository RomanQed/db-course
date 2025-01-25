package com.github.romanqed.course.integrations;

import com.github.romanqed.course.MockUtil;
import com.github.romanqed.course.controllers.AuthController;
import com.github.romanqed.course.controllers.UserController;
import com.github.romanqed.course.dto.Token;
import com.github.romanqed.course.models.User;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class AuthorizationITCase {
    private static String database;
    private static Connection connection;
    private static AuthController auth;
    private static UserController users;

    @BeforeAll
    public static void init() throws Throwable {
        database = Util.getRandomDb("auth");
        connection = Util.initDatabase(database, List.of(Util.TABLES, Util.ROLES));
        var encoder = Util.createEncoder();
        var jwt = Util.createJwtProvider();
        var userRepo = Util.initUserRepo(connection, encoder);
        var telemetry = OpenTelemetry.noop();
        auth = new AuthController(
                userRepo,
                jwt,
                encoder,
                null,
                telemetry
        );
        users = Util.createUserController(jwt, encoder, userRepo, telemetry);
    }

    @AfterAll
    public static void destroy() throws SQLException {
        connection.close();
        Util.dropDatabase(database);
    }

    @Test
    public void test() {
        // Register new user
        var ctx = MockUtil.ctxBuilder()
                .withBody(Util.ofCreds("myuser", "1234pass"))
                .build();
        auth.register(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var token = ((Token) ctx.body).getToken();
        // Get user by token
        ctx = MockUtil.ctxBuilder()
                .withAuth(token)
                .build();
        users.getSelf(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var user = (User) ctx.body;
        assertEquals("myuser", user.getLogin());
        assertFalse(user.isAdmin());
        // Login with admin user
        ctx = MockUtil.ctxBuilder()
                .withBody(Util.ofCreds("admin", "pass"))
                .build();
        auth.login(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        token = ((Token) ctx.body).getToken();
        // Get admin by token
        ctx = MockUtil.ctxBuilder()
                .withAuth(token)
                .build();
        users.getSelf(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        user = (User) ctx.body;
        assertEquals("admin", user.getLogin());
        assertTrue(user.isAdmin());
    }
}
