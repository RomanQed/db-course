package com.github.romanqed.course.units;

import com.github.romanqed.course.MockUtil;
import com.github.romanqed.course.controllers.ExchangeController;
import com.github.romanqed.course.dto.ExchangeDto;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.Exchange;
import com.github.romanqed.course.models.User;
import com.github.romanqed.course.otel.OtelUtil;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public final class ExchangeControllerTest {
    private static final OpenTelemetry TELEMETRY = OtelUtil.createOtel();

    @Test
    public void testGet() {
        var es = new RepositoryImpl<Exchange>() {
            @Override
            public Exchange get(String role, int key) {
                var ret = new Exchange();
                ret.setId(key);
                return ret;
            }
        };
        var ct = new ExchangeController(null, null, es, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/exchanges/10")
                .withPath("id", 10)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(10, ((Exchange) ctx.body).getId());
    }

    @Test
    public void testGetNotExisting() {
        var es = new RepositoryImpl<Exchange>();
        var ct = new ExchangeController(null, null, es, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/exchanges/10")
                .withPath("id", 10)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.NOT_FOUND, ctx.status);
    }

    @Test
    public void testFind() {
        var lst = new ArrayList<Exchange>();
        var es = new RepositoryImpl<Exchange>() {
            String filter;

            @Override
            public List<Exchange> get(String role, String where) {
                filter = where;
                return lst;
            }
        };
        var ct = new ExchangeController(null, null, es, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/exchanges")
                .withQuery("from", 0)
                .withQuery("to", 1)
                .build();

        ct.find(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(lst, ctx.body);
        assertEquals("_from = 0 and _to = 1", es.filter);
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
        var es = new RepositoryImpl<Exchange>() {
            final List<Exchange> l = new ArrayList<>();

            @Override
            public void put(String role, Exchange model) {
                l.add(model);
            }
        };
        var cs = new RepositoryImpl<Currency>() {
            @Override
            public boolean exists(String role, int id) {
                return true;
            }
        };
        var ct = new ExchangeController(jwt, users, es, cs, TELEMETRY);
        var dto = new ExchangeDto();
        dto.setFrom(0);
        dto.setTo(1);
        dto.setFactor(100.0);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/exchanges")
                .withBody(dto)
                .withAuth()
                .build();

        ct.post(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(2, es.l.size());
        assertEquals(2, ((List) ctx.body).size());
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
        var es = new RepositoryImpl<Exchange>();
        var ct = new ExchangeController(jwt, users, es, null, TELEMETRY);
        var dto = new ExchangeDto();
        dto.setFrom(0);
        dto.setTo(1);
        dto.setFactor(100.0);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/exchanges")
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
        var es = new RepositoryImpl<Exchange>() {
            final List<Exchange> l = new ArrayList<>();

            @Override
            public Exchange get(String role, int key) {
                var ret = new Exchange();
                ret.setId(key);
                return ret;
            }

            @Override
            public List<Exchange> get(String role, String where) {
                var e1 = new Exchange();
                e1.setFactor(100);
                e1.setFrom(0);
                e1.setTo(1);
                var e2 = new Exchange();
                e2.setFactor(0.01);
                e2.setFrom(1);
                e2.setTo(0);
                return List.of(e1, e2);
            }

            @Override
            public void update(String role, Exchange model) {
                l.add(model);
            }
        };
        var ct = new ExchangeController(jwt, users, es, null, TELEMETRY);
        var dto = new ExchangeDto();
        dto.setFactor(50.0);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.PATCH)
                .withURI("/exchanges/13")
                .withPath("id", 13)
                .withBody(dto)
                .withAuth()
                .build();

        ct.update(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        int cnt = 0;
        for (var e : es.l) {
            if (e.getFactor() == 50.0) {
                ++cnt;
            }
            if (e.getFactor() == (1 / 50.0)) {
                ++cnt;
            }
        }
        assertEquals(2, cnt);
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
        var es = new RepositoryImpl<Exchange>();
        var ct = new ExchangeController(jwt, users, es, null, TELEMETRY);
        var dto = new ExchangeDto();
        dto.setFactor(100.0);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.PATCH)
                .withURI("/exchanges/13")
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
        var es = new RepositoryImpl<Exchange>() {
            final List<Integer> l = new ArrayList<>();

            @Override
            public void delete(String role, int key) {
                l.add(key);
            }

            @Override
            public Exchange get(String role, int key) {
                var ret = new Exchange();
                ret.setId(14);
                return ret;
            }

            @Override
            public List<Exchange> get(String role, String where) {
                var ret = new Exchange();
                ret.setId(15);
                return List.of(ret);
            }
        };
        var ct = new ExchangeController(jwt, users, es, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.DELETE)
                .withURI("/exchanges/14")
                .withPath("id", 14)
                .withAuth()
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertIterableEquals(List.of(14, 15), es.l);
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
        var es = new RepositoryImpl<Exchange>() {
            @Override
            public Exchange get(String role, int key) {
                var ret = new Exchange();
                ret.setId(key);
                return ret;
            }
        };
        var ct = new ExchangeController(jwt, users, es, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.DELETE)
                .withURI("/exchanges/14")
                .withPath("id", 14)
                .withAuth()
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.FORBIDDEN, ctx.status);
    }
}
