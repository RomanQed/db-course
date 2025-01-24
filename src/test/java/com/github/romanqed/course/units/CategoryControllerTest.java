package com.github.romanqed.course.units;

import com.github.romanqed.course.MockUtil;
import com.github.romanqed.course.controllers.CategoryController;
import com.github.romanqed.course.dto.NameDto;
import com.github.romanqed.course.models.Category;
import com.github.romanqed.course.models.Transaction;
import com.github.romanqed.course.models.User;
import com.github.romanqed.course.otel.OtelUtil;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CategoryControllerTest {
    private static final OpenTelemetry TELEMETRY = OtelUtil.createOtel();

    @Test
    public void testListTransactions() {
        var jwt = MockUtil.mockProvider(12);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                return User.of("usr", "ps");
            }
        };
        var cats = new RepositoryImpl<Category>() {
            @Override
            public Category get(String role, int key) {
                var ret = new Category();
                ret.setId(key);
                return ret;
            }
        };
        var lst = new ArrayList<Transaction>();
        var trs = new RepositoryImpl<Transaction>() {
            String where;

            @Override
            public List<Transaction> get(String role, String where) {
                this.where = where;
                return lst;
            }
        };
        var ct = new CategoryController(jwt, users, cats, trs, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/categories/2/transactions")
                .withQuery("from", "2021-01-01")
                .withAuth()
                .withPath("id", 2)
                .build();

        ct.listTransactions(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(lst, ctx.body);
        assertEquals("category = 2 and owner = 0 and _timestamp > '2021-01-01 00:00:00'", trs.where);
    }

    @Test
    public void testListTransactionsWithInvalidRange() {
        var jwt = MockUtil.mockProvider(12);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                return User.of("usr", "ps");
            }
        };
        var cats = new RepositoryImpl<Category>() {
            @Override
            public Category get(String role, int key) {
                var ret = new Category();
                ret.setId(key);
                return ret;
            }
        };
        var ct = new CategoryController(jwt, users, cats, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/categories/2/transactions")
                .withQuery("from", "2021-01-01")
                .withQuery("to", "2020-01-01")
                .withAuth()
                .withPath("id", 2)
                .build();

        ct.listTransactions(ctx.mock);

        assertEquals(HttpStatus.BAD_REQUEST, ctx.status);
    }

    @Test
    public void testGet() {
        var cats = new RepositoryImpl<Category>() {
            @Override
            public Category get(String role, int key) {
                var ret = new Category();
                ret.setId(key);
                return ret;
            }
        };
        var ct = new CategoryController(null, null, cats, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/categories/2")
                .withPath("id", 10)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(10, ((Category) ctx.body).getId());
    }

    @Test
    public void testGetNotExisting() {
        var cats = new RepositoryImpl<Category>();
        var ct = new CategoryController(null, null, cats, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/categories/10")
                .withPath("id", 10)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.NOT_FOUND, ctx.status);
    }

    @Test
    public void testFind() {
        var lst = new ArrayList<Category>();
        var cats = new RepositoryImpl<Category>() {
            @Override
            public List<Category> get(String role) {
                return lst;
            }
        };
        var ct = new CategoryController(null, null, cats, null, TELEMETRY);
        var ctx = MockUtil.mockCtx(HandlerType.GET, "/categories");

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
        var cats = new RepositoryImpl<Category>() {
            Category cat;

            @Override
            public void put(String role, Category model) {
                cat = model;
            }
        };
        var ct = new CategoryController(jwt, users, cats, null, TELEMETRY);
        var dto = new NameDto();
        dto.setName("tcat1");
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/categories")
                .withBody(dto)
                .withAuth()
                .build();

        ct.post(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals("tcat1", cats.cat.getName());
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
        var cats = new RepositoryImpl<Category>();
        var ct = new CategoryController(jwt, users, cats, null, TELEMETRY);
        var dto = new NameDto();
        dto.setName("tcat1");
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/categories")
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
        var cats = new RepositoryImpl<Category>() {
            Category cat;

            @Override
            public Category get(String role, int key) {
                var ret = new Category();
                ret.setId(key);
                return ret;
            }

            @Override
            public void update(String role, Category model) {
                cat = model;
            }
        };
        var ct = new CategoryController(jwt, users, cats, null, TELEMETRY);
        var dto = new NameDto();
        dto.setName("tcat2");
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.PATCH)
                .withURI("/categories/13")
                .withPath("id", 13)
                .withBody(dto)
                .withAuth()
                .build();

        ct.update(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(13, cats.cat.getId());
        assertEquals("tcat2", cats.cat.getName());
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
        var cats = new RepositoryImpl<Category>();
        var ct = new CategoryController(jwt, users, cats, null, TELEMETRY);
        var dto = new NameDto();
        dto.setName("tcat1");
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.PATCH)
                .withURI("/categories/13")
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
        var cats = new RepositoryImpl<Category>() {
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
        var ct = new CategoryController(jwt, users, cats, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.DELETE)
                .withURI("/categories/14")
                .withPath("id", 14)
                .withAuth()
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(14, cats.id);
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
        var cats = new RepositoryImpl<Category>();
        var ct = new CategoryController(jwt, users, cats, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.DELETE)
                .withURI("/categories/14")
                .withPath("id", 14)
                .withAuth()
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.FORBIDDEN, ctx.status);
    }
}
