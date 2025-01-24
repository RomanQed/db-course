package com.github.romanqed.course.units;

import com.github.romanqed.course.MockUtil;
import com.github.romanqed.course.controllers.CurrencyController;
import com.github.romanqed.course.dto.NameDto;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.User;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CurrencyControllerTest {

    @Test
    public void testGet() {
        var curs = new RepositoryImpl<Currency>() {
            @Override
            public Currency get(String role, int key) {
                var ret = new Currency();
                ret.setId(key);
                return ret;
            }
        };
        var ct = new CurrencyController(null, null, curs);
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 10)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(10, ((Currency) ctx.body).getId());
    }

    @Test
    public void testGetNotExisting() {
        var curs = new RepositoryImpl<Currency>();
        var ct = new CurrencyController(null, null, curs);
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 10)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.NOT_FOUND, ctx.status);
    }

    @Test
    public void testFind() {
        var lst = new ArrayList<Currency>();
        var curs = new RepositoryImpl<Currency>() {
            @Override
            public List<Currency> get(String role) {
                return lst;
            }
        };
        var ct = new CurrencyController(null, null, curs);
        var ctx = MockUtil.mockCtx(HandlerType.GET, "/currencies");

        ct.find(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(lst, ctx.body);
    }

    @Test
    public void testPost() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setAdmin(true);
                return ret;
            }
        };
        var cs = new RepositoryImpl<Currency>() {
            Currency c;

            @Override
            public void put(String role, Currency model) {
                c = model;
            }
        };
        var ct = new CurrencyController(jwt, users, cs);
        var dto = new NameDto();
        dto.setName("tcur1");
        var ctx = MockUtil.ctxBuilder()
                .withBody(dto)
                .withAuth()
                .build();

        ct.post(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals("tcur1", cs.c.getName());
    }

    @Test
    public void testPostAsNotAdmin() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                return new User();
            }
        };
        var cs = new RepositoryImpl<Currency>();
        var ct = new CurrencyController(jwt, users, cs);
        var dto = new NameDto();
        dto.setName("tcur1");
        var ctx = MockUtil.ctxBuilder()
                .withBody(dto)
                .withAuth()
                .build();

        ct.post(ctx.mock);

        assertEquals(HttpStatus.FORBIDDEN, ctx.status);
    }

    @Test
    public void testUpdate() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setAdmin(true);
                return ret;
            }
        };
        var cs = new RepositoryImpl<Currency>() {
            Currency c;

            @Override
            public Currency get(String role, int key) {
                var ret = new Currency();
                ret.setId(key);
                return ret;
            }

            @Override
            public void update(String role, Currency model) {
                c = model;
            }
        };
        var ct = new CurrencyController(jwt, users, cs);
        var dto = new NameDto();
        dto.setName("tc2");
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 13)
                .withBody(dto)
                .withAuth()
                .build();

        ct.update(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(13, cs.c.getId());
        assertEquals("tc2", cs.c.getName());
    }

    @Test
    public void testUpdateAsNotAdmin() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                return new User();
            }
        };
        var cs = new RepositoryImpl<Currency>();
        var ct = new CurrencyController(jwt, users, cs);
        var dto = new NameDto();
        dto.setName("tc1");
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 13)
                .withBody(dto)
                .withAuth()
                .build();

        ct.update(ctx.mock);

        assertEquals(HttpStatus.FORBIDDEN, ctx.status);
    }

    @Test
    public void testDelete() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setAdmin(true);
                return ret;
            }
        };
        var cs = new RepositoryImpl<Currency>() {
            int id;

            @Override
            public void delete(String role, int key) {
                id = key;
            }

            @Override
            public boolean exists(String role, int id) {
                return id == 14;
            }
        };
        var ct = new CurrencyController(jwt, users, cs);
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 14)
                .withAuth()
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(14, cs.id);
    }

    @Test
    public void testDeleteAsNotAdmin() {
        var jwt = MockUtil.mockProvider(0);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                return new User();
            }
        };
        var cs = new RepositoryImpl<Currency>();
        var ct = new CurrencyController(jwt, users, cs);
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 14)
                .withAuth()
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.FORBIDDEN, ctx.status);
    }
}
