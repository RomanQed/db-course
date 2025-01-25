package com.github.romanqed.course.integrations;

import com.github.romanqed.course.MockUtil;
import com.github.romanqed.course.controllers.AuthController;
import com.github.romanqed.course.dto.Token;
import com.github.romanqed.course.dto.TwoFactorDto;
import com.github.romanqed.course.email.LocalMailerStub;
import com.github.romanqed.course.models.User;
import com.google.gson.Gson;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public final class TwoFactorITCase {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final URI SMTP_FAKE_SERVER = URI.create(System.getenv("SMTP_FAKE_SERVER"));
    private static final Gson GSON = new Gson();
    private static String database;
    private static Connection connection;
    private static AuthController auth;
    private static String email;
    private static int emailId = -1;

    private static <T> T from(HttpResponse<String> response, Class<T> type) {
        var code = response.statusCode();
        if (code != 200) {
            throw new IllegalStateException("Unexpected response code: " + code);
        }
        var raw = response.body();
        return GSON.fromJson(raw, type);
    }

    private static String requestCode() throws IOException, InterruptedException {
        var req = HttpRequest.newBuilder()
                .uri(SMTP_FAKE_SERVER.resolve("/api/emails"))
                .GET()
                .build();
        var rsp = from(CLIENT.send(req, HttpResponse.BodyHandlers.ofString()), Response.class);
        for (var mail : rsp.content) {
            if (email.equals(mail.toAddress)) {
                emailId = mail.id;
                return mail.contents.get(0).data;
            }
        }
        throw new IllegalStateException("Email not found");
    }

    private static String getRandomUserEmail() {
        var uuid = UUID.randomUUID();
        var raw = uuid.toString().replace("-", "");
        return raw + "@email.com";
    }

    @BeforeAll
    public static void init() throws Throwable {
        database = Util.getRandomDb("auth");
        connection = Util.initDatabase(database, List.of(Util.TABLES, Util.ROLES));
        var encoder = Util.createEncoder();
        var jwt = Util.createJwtProvider();
        var userRepo = Util.initUserRepo(connection, encoder);
        var user = new User();
        user.setLogin("user");
        user.setPassword(encoder.encode("123"));
        user.setTwoFactor(true);
        email = getRandomUserEmail();
        user.setEmail(email);
        user.setAdmin(false);
        userRepo.put(Util.SYSTEM_ROLE, user);
        auth = new AuthController(
                userRepo,
                jwt,
                encoder,
                new LocalMailerStub(),
                Otel.TELEMETRY
        );
    }

    @AfterAll
    public static void destroy() throws SQLException, IOException, InterruptedException {
        connection.close();
        Util.dropDatabase(database);
        if (emailId >= 0) {
            var req = HttpRequest.newBuilder()
                    .uri(SMTP_FAKE_SERVER.resolve("/api/emails/" + emailId))
                    .DELETE()
                    .build();
            CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        }
    }

    @Test
    public void test() throws IOException, InterruptedException {
        // Login
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/login")
                .withBody(Util.ofCreds("user", "123"))
                .build();
        auth.login(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var token = ((Token) ctx.body);
        assertTrue(token.getTwoFactor());
        // Get 2fa code from mail server
        var code = requestCode();
        // Login with code
        var dto = new TwoFactorDto();
        dto.setLogin("user");
        dto.setCode(code);
        ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/2fa")
                .withBody(dto)
                .build();
        auth.login2Fa(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        assertNotNull(((Token) ctx.body).getToken());
    }

    private static final class Response {
        public List<Email> content;
    }

    private static final class Email {
        public Integer id;
        public String toAddress;
        public List<Content> contents;
    }

    private static final class Content {
        public String data;
    }
}
