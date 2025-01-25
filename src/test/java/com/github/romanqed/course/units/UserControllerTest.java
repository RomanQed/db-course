package com.github.romanqed.course.units;

import com.github.romanqed.course.MockUtil;
import com.github.romanqed.course.controllers.UserController;
import com.github.romanqed.course.dto.UserUpdateDto;
import com.github.romanqed.course.models.Account;
import com.github.romanqed.course.models.Budget;
import com.github.romanqed.course.models.Transaction;
import com.github.romanqed.course.models.User;
import com.github.romanqed.course.otel.OtelUtil;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class UserControllerTest {
    private static final OpenTelemetry TELEMETRY = OtelUtil.createOtel("UnitTests");

    @Test
    public void testGetSelf() {
        var jwt = MockUtil.mockProvider(24);
        var users = new RepositoryImpl<User>() {
            int id;

            @Override
            public User get(String role, int key) {
                var user = new User();
                user.setId(key);
                id = key;
                return user;
            }
        };
        var ct = new UserController(jwt, users, null, null, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/users")
                .withAuth()
                .build();

        ct.getSelf(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(24, ((User) ctx.body).getId());
    }

    @Test
    public void testGetSelfUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new UserController(jwt, null, null, null, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/users")
                .withPath("id", 0)
                .build();

        ct.getSelf(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testGet() {
        var jwt = MockUtil.mockProvider(24);
        var users = new RepositoryImpl<User>() {
            int id;

            @Override
            public User get(String role, int key) {
                var user = new User();
                user.setId(key);
                id = key;
                return user;
            }
        };
        var ct = new UserController(jwt, users, null, null, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/users/24")
                .withAuth()
                .withPath("id", 24)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(24, ((User) ctx.body).getId());
    }

    @Test
    public void testGetUnauthorized() {
        var jwt = MockUtil.mockProvider(24);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var user = new User();
                user.setId(key);
                return user;
            }
        };
        var ct = new UserController(jwt, users, null, null, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/users/24")
                .withPath("id", 24)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.FORBIDDEN, ctx.status);
    }

    @Test
    public void testUpdateSelf() {
        var jwt = MockUtil.mockProvider(24);
        var users = new RepositoryImpl<User>() {
            User u;

            @Override
            public User get(String role, int key) {
                var user = new User();
                user.setId(24);
                return user;
            }

            @Override
            public void update(String role, User model) {
                u = model;
            }
        };
        var encoder = new EncoderImpl();
        var ct = new UserController(jwt, users, null, null, null, encoder, TELEMETRY);
        var dto = new UserUpdateDto();
        dto.setPassword("newpass");
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.PATCH)
                .withURI("/users")
                .withBody(dto)
                .withAuth()
                .build();

        ct.updateSelf(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(24, users.u.getId());
        assertEquals("newpass", users.u.getPassword());
    }

    @Test
    public void testUpdateSelfUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new UserController(jwt, null, null, null, null, null, TELEMETRY);
        var dto = new UserUpdateDto();
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.PATCH)
                .withURI("/users")
                .withBody(dto)
                .build();

        ct.updateSelf(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testUpdate() {
        var jwt = MockUtil.mockProvider(24);
        var users = new RepositoryImpl<User>() {
            User u;

            @Override
            public User get(String role, int key) {
                var user = new User();
                user.setId(24);
                return user;
            }

            @Override
            public void update(String role, User model) {
                u = model;
            }
        };
        var encoder = new EncoderImpl();
        var ct = new UserController(jwt, users, null, null, null, encoder, TELEMETRY);
        var dto = new UserUpdateDto();
        dto.setPassword("newpass");
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.PATCH)
                .withURI("/users/24")
                .withPath("id", 24)
                .withBody(dto)
                .withAuth()
                .build();

        ct.update(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(24, users.u.getId());
        assertEquals("newpass", users.u.getPassword());
    }

    @Test
    public void testUpdateUnauthorized() {
        var jwt = MockUtil.mockProvider(24);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var user = new User();
                user.setId(24);
                return user;
            }
        };
        var ct = new UserController(jwt, users, null, null, null, null, TELEMETRY);
        var dto = new UserUpdateDto();
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.PATCH)
                .withURI("/users/24")
                .withPath("id", 24)
                .withBody(dto)
                .build();

        ct.update(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testListAccounts() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setId(key);
                return ret;
            }

            @Override
            public boolean exists(String role, int id) {
                return id == 0;
            }
        };
        var a = new Account();
        var l = List.of(a);
        var acs = new RepositoryImpl<Account>() {
            @Override
            public List<Account> get(String role, String field, Object value) {
                return l;
            }
        };
        var ct = new UserController(jwt, users, acs, null, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/users/0/accounts")
                .withAuth()
                .withPath("id", 0)
                .build();

        ct.listAccounts(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(l, ctx.body);
    }

    @Test
    public void testListAccountsUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new UserController(jwt, null, null, null, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/users/0/accounts")
                .withPath("id", 0)
                .build();

        ct.listAccounts(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testListBudgets() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setId(key);
                return ret;
            }

            @Override
            public boolean exists(String role, int id) {
                return id == 0;
            }
        };
        var b = new Budget();
        var l = List.of(b);
        var bds = new RepositoryImpl<Budget>() {
            @Override
            public List<Budget> get(String role, String field, Object value) {
                return l;
            }
        };
        var ct = new UserController(jwt, users, null, bds, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/users/0/budgets")
                .withAuth()
                .withPath("id", 0)
                .build();

        ct.listBudgets(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(l, ctx.body);
    }

    @Test
    public void testListBudgetsUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new UserController(jwt, null, null, null, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/users/0/budgets")
                .withPath("id", 0)
                .build();

        ct.listBudgets(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testListTransactions() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setId(key);
                return ret;
            }

            @Override
            public boolean exists(String role, int id) {
                return id == 0;
            }
        };
        var t = new Transaction();
        var l = List.of(t);
        var ts = new RepositoryImpl<Transaction>() {
            @Override
            public List<Transaction> get(String role, String field, Object value) {
                return l;
            }
        };
        var ct = new UserController(jwt, users, null, null, ts, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/users/0/transactions")
                .withAuth()
                .withPath("id", 0)
                .build();

        ct.listTransactions(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(l, ctx.body);
    }

    @Test
    public void testListTransactionsUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new UserController(jwt, null, null, null, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/users/0/transactions")
                .withPath("id", 0)
                .build();

        ct.listTransactions(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }
}
