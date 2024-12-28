package com.github.romanqed.course;

import com.github.romanqed.course.controllers.AccountController;
import com.github.romanqed.course.dto.AccountDto;
import com.github.romanqed.course.models.Account;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.User;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class AccountControllerTest {

    @Test
    public void testGet() {
        var jwt = MockUtil.mockProvider(15);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = User.of("usr", "ps");
                ret.setId(key);
                return ret;
            }
        };
        var acc = new Account();
        acc.setOwner(15);
        acc.setId(1);
        var acs = new RepositoryImpl<Account>() {
            @Override
            public Account get(String role, int key) {
                return acc;
            }
        };
        var ct = new AccountController(jwt, users, acs, null);
        var ctx = MockUtil.ctxBuilder()
                .withAuth()
                .withPath("id", 1)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(acc, ctx.body);
    }

    @Test
    public void testGetUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new AccountController(jwt, null, null, null);
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 1)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testPost() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = User.of("usr", "ps");
                ret.setId(key);
                return ret;
            }
        };
        var acs = new RepositoryImpl<Account>() {
            Account acc;

            @Override
            public void put(String role, Account model) {
                acc = model;
            }
        };
        var curs = new RepositoryImpl<Currency>() {
            @Override
            public boolean exists(String role, int id) {
                return id == 1;
            }
        };
        var ct = new AccountController(jwt, users, acs, curs);
        var dto = new AccountDto();
        dto.setDescription("descr");
        dto.setCurrency(1);
        dto.setValue(151.0);
        var ctx = MockUtil.ctxBuilder()
                .withAuth()
                .withBody(dto)
                .build();

        ct.post(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertNotNull(ctx.body);
        assertNotNull(acs.acc);
        var acc = (Account) ctx.body;
        assertEquals("descr", acc.getDescription());
        assertEquals(1, acc.getCurrency());
        assertEquals(151.0, acc.getValue());
        assertEquals("descr", acs.acc.getDescription());
        assertEquals(1, acs.acc.getCurrency());
        assertEquals(151.0, acs.acc.getValue());
    }

    @Test
    public void testPostUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new AccountController(jwt, null, null, null);
        var dto = new AccountDto();
        dto.setDescription("descr");
        dto.setCurrency(1);
        dto.setValue(15.0);
        var ctx = MockUtil.ctxBuilder()
                .withBody(dto)
                .build();

        ct.post(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testUpdate() {
        var jwt = MockUtil.mockProvider(15);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = User.of("usr", "ps");
                ret.setId(key);
                return ret;
            }
        };
        var acc = new Account();
        acc.setOwner(15);
        acc.setDescription("d");
        acc.setCurrency(1);
        acc.setValue(5.0);
        acc.setId(1014);
        var acs = new RepositoryImpl<Account>() {
            Account upd;

            @Override
            public void update(String role, Account model) {
                upd = model;
            }

            @Override
            public Account get(String role, int key) {
                if (key == 1014) {
                    return acc;
                }
                return null;
            }
        };
        var curs = new RepositoryImpl<Currency>() {
            @Override
            public boolean exists(String role, int id) {
                return id == 1 || id == 2;
            }
        };
        var ct = new AccountController(jwt, users, acs, curs);
        var dto = new AccountDto();
        dto.setDescription("descr");
        dto.setCurrency(2);
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 1014)
                .withAuth()
                .withBody(dto)
                .build();

        ct.update(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(acc, acs.upd);
        assertEquals(1014, acc.getId());
        assertEquals("descr", acc.getDescription());
        assertEquals(2, acc.getCurrency());
        assertEquals(5, acc.getValue());
    }

    @Test
    public void testUpdateUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new AccountController(jwt, null, null, null);
        var dto = new AccountDto();
        dto.setDescription("descr");
        dto.setCurrency(1);
        dto.setValue(15.0);
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 1)
                .withBody(dto)
                .build();

        ct.update(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testDelete() {
        var jwt = MockUtil.mockProvider(15);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = User.of("usr", "ps");
                ret.setId(key);
                return ret;
            }
        };
        var acc = new Account();
        acc.setId(13);
        acc.setOwner(15);
        var acs = new RepositoryImpl<Account>() {
            int id;

            @Override
            public Account get(String role, int key) {
                if (key == 13) {
                    return acc;
                }
                return null;
            }

            @Override
            public void delete(String role, int key) {
                id = key;
            }
        };
        var ct = new AccountController(jwt, users, acs, null);
        var ctx = MockUtil.ctxBuilder()
                .withAuth()
                .withPath("id", 13)
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(13, acs.id);
    }

    @Test
    public void testDeleteUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new AccountController(jwt, null, null, null);
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 1)
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }
}
