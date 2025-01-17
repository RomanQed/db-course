package com.github.romanqed.course.integrations;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.romanqed.course.controllers.UserController;
import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.Credentials;
import com.github.romanqed.course.dto.NameDto;
import com.github.romanqed.course.hash.Encoder;
import com.github.romanqed.course.hash.PBKDF2Encoder;
import com.github.romanqed.course.jwt.JavalinJwtProvider;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.User;
import com.github.romanqed.course.postgres.PostgresRepository;
import com.github.romanqed.jfunc.Function1;
import com.github.romanqed.jfunc.Runnable1;
import javalinjwt.JWTGenerator;
import javalinjwt.JWTProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class Util {
    public static final String BUDGETS = "/budget_tools.sql";
    public static final String TRANSACTIONS = "/transaction_tools.sql";
    public static final String TABLES = "/tables.sql";
    public static final String ROLES = "/roles.sql";
    public static final String USER = "postgres";
    public static final String PASSWORD = "postgres";
    public static final String URL = System.getenv("DB_CONN");
    public static final String SYSTEM_ROLE = "_service";
    private static final Class<?> CLASS = Util.class;

    private Util() {
    }

    public static String getRandomDb(String base) {
        var uuid = UUID.randomUUID();
        var raw = uuid.toString().replace("-", "");
        return base + raw;
    }

    public static Connection initDatabase(String database, List<String> resources) throws Throwable {
        var master = DriverManager.getConnection(URL + "/?user=" + USER + "&password=" + PASSWORD);
        execute(master, "create database " + database);
        master.close();
        var ret = DriverManager.getConnection(URL + "/" + database + "?user=" + USER + "&password=" + PASSWORD);
        for (var resource : resources) {
            execute(ret, readResource(resource));
        }
        return ret;
    }

    public static Repository<User> initUserRepo(Connection connection, Encoder encoder) {
        var ret = new PostgresRepository<>(
                connection,
                "users",
                User::new,
                (setter, obj) -> User.to(setter, (User) obj),
                (getter, obj) -> User.from(getter, (User) obj)
        );
        var user = new User();
        user.setLogin("admin");
        user.setPassword(encoder.encode("pass"));
        user.setAdmin(true);
        ret.put(SYSTEM_ROLE, user);
        return ret;
    }

    private static byte[] generateSalt() {
        var random = new SecureRandom();
        var ret = new byte[16];
        random.nextBytes(ret);
        return ret;
    }

    public static Encoder createEncoder() {
        return new PBKDF2Encoder(generateSalt());
    }

    private static Date getExpirationTime() {
        var calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        return calendar.getTime();
    }

    public static JwtProvider<JwtUser> createJwtProvider() {
        var hmac = Algorithm.HMAC256("secret");
        var generator = (JWTGenerator<JwtUser>) (user, algorithm) -> {
            var token = JWT.create()
                    .withClaim("id", user.getId())
                    .withClaim("login", user.getLogin())
                    .withClaim("admin", user.isAdmin())
                    .withExpiresAt(getExpirationTime());
            return token.sign(algorithm);
        };
        var verifier = JWT.require(hmac).build();
        var jwtProvider = new JWTProvider<>(hmac, generator, verifier);
        return new JavalinJwtProvider<>(jwtProvider);
    }

    public static UserController createUserController(JwtProvider<JwtUser> jwt,
                                                      Encoder encoder,
                                                      Repository<User> users) {
        return new UserController(
                jwt,
                users,
                null,
                null,
                null,
                encoder
        );
    }

    public static Credentials ofCreds(String login, String pass) {
        var ret = new Credentials();
        ret.setLogin(login);
        ret.setPassword(pass);
        return ret;
    }

    public static NameDto ofName(String name) {
        var ret = new NameDto();
        ret.setName(name);
        return ret;
    }

    public static void dropDatabase(String database) throws SQLException {
        var master = DriverManager.getConnection(URL + "/?user=" + USER + "&password=" + PASSWORD);
        execute(master, "drop database " + database);
        master.close();
    }

    public static void execute(Connection c, String sql) throws SQLException {
        var statement = c.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    public static void query(Function1<String, PreparedStatement> f,
                             String sql,
                             List<Object> args,
                             Runnable1<ResultSet> consumer) throws Throwable {
        var statement = f.invoke(sql);
        var i = 1;
        for (var arg : args) {
            if (arg instanceof Date) {
                statement.setObject(i++, arg, Types.TIMESTAMP);
            } else {
                statement.setObject(i++, arg);
            }
        }
        var set = statement.executeQuery();
        if (!set.next()) {
            throw new IllegalStateException();
        }
        consumer.run(set);
        statement.close();
    }

    public static void update(Function1<String, PreparedStatement> f, String sql, List<Object> args) throws Throwable {
        var statement = f.invoke(sql);
        var i = 1;
        for (var arg : args) {
            if (arg instanceof Date) {
                statement.setObject(i++, arg, Types.TIMESTAMP);
            } else {
                statement.setObject(i++, arg);
            }
        }
        statement.executeUpdate();
        statement.close();
    }

    public static String readResource(String name) throws IOException {
        var stream = CLASS.getResourceAsStream(name);
        if (stream == null) {
            throw new IllegalStateException("Resource not found");
        }
        var reader = new BufferedReader(new InputStreamReader(stream));
        var ret = reader.lines().reduce("", (p, n) -> p + n + "\n");
        reader.close();
        stream.close();
        return ret;
    }
}
