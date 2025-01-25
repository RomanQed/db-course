package com.github.romanqed.course.units;

import com.github.romanqed.course.MockUtil;
import com.github.romanqed.course.TimeUtil;
import com.github.romanqed.course.controllers.BudgetController;
import com.github.romanqed.course.dto.BudgetDto;
import com.github.romanqed.course.dto.BudgetStatus;
import com.github.romanqed.course.models.Budget;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.User;
import com.github.romanqed.course.otel.OtelUtil;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public final class BudgetControllerTest {
    private static final OpenTelemetry TELEMETRY = OtelUtil.createOtel("UnitTests");

    @Test
    public void testStatus() throws SQLException {
        var conn = Mockito.mock(Connection.class);
        var st = Mockito.mock(Statement.class);
        var obj = new PGobject();
        obj.setValue("(1,2,3)");
        var rs = new ResultSetImpl() {
            @Override
            public boolean next() {
                return true;
            }

            @Override
            public <T> T getObject(int columnIndex, Class<T> type) {
                return (T) obj;
            }
        };
        Mockito
                .when(st.executeQuery(Mockito.anyString()))
                .thenReturn(rs);
        Mockito.when(conn.createStatement())
                .thenReturn(st);
        var jwt = MockUtil.mockProvider(15);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setId(key);
                return ret;
            }
        };
        var budgets = new RepositoryImpl<Budget>() {
            @Override
            public Budget get(String role, int key) {
                if (key != 1) {
                    return null;
                }
                var ret = new Budget();
                ret.setOwner(15);
                return ret;
            }
        };
        var ct = new BudgetController(jwt, conn, users, budgets, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/budgets/1/status")
                .withAuth()
                .withPath("id", 1)
                .build();

        ct.status(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        var status = (BudgetStatus) ctx.body;
        assertEquals(1, status.getSpent());
        assertEquals(2, status.getGot());
        assertEquals(3, status.getTotal());
    }

    @Test
    public void testStatusFailed() throws SQLException {
        var conn = Mockito.mock(Connection.class);
        var st = Mockito.mock(Statement.class);
        var rs = new ResultSetImpl() {
            @Override
            public boolean next() {
                return false;
            }
        };
        Mockito
                .when(st.executeQuery(Mockito.anyString()))
                .thenReturn(rs);
        Mockito.when(conn.createStatement())
                .thenReturn(st);
        var jwt = MockUtil.mockProvider(15);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setId(key);
                return ret;
            }
        };
        var budgets = new RepositoryImpl<Budget>() {
            @Override
            public Budget get(String role, int key) {
                if (key != 1) {
                    return null;
                }
                var ret = new Budget();
                ret.setOwner(15);
                return ret;
            }
        };
        var ct = new BudgetController(jwt, conn, users, budgets, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/budgets/1/status")
                .withAuth()
                .withPath("id", 1)
                .build();

        assertThrows(IllegalStateException.class, () -> ct.status(ctx.mock));
    }

    @Test
    public void testGet() {
        var jwt = MockUtil.mockProvider(15);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setId(key);
                return ret;
            }
        };
        var bdg = new Budget();
        bdg.setOwner(15);
        bdg.setId(1);
        var bds = new RepositoryImpl<Budget>() {
            @Override
            public Budget get(String role, int key) {
                return bdg;
            }
        };
        var ct = new BudgetController(jwt, null, users, bds, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/budgets/1")
                .withAuth()
                .withPath("id", 1)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(bdg, ctx.body);
    }

    @Test
    public void testGetUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new BudgetController(jwt, null, null, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/budgets/1")
                .withPath("id", 1)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testPost() {
        var jwt = MockUtil.mockProvider(15);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = User.of("usr", "ps");
                ret.setId(key);
                return ret;
            }
        };
        var bs = new RepositoryImpl<Budget>() {
            Budget b;

            @Override
            public void put(String role, Budget model) {
                b = model;
            }
        };
        var curs = new RepositoryImpl<Currency>() {
            @Override
            public boolean exists(String role, int id) {
                return id == 1;
            }
        };
        var ct = new BudgetController(jwt, null, users, bs, curs, TELEMETRY);
        var dto = new BudgetDto();
        dto.setDescription("descr");
        dto.setCurrency(1);
        dto.setValue(151.0);
        var start = TimeUtil.nullifyTime(new Date());
        var end = (Date) start.clone();
        end.setYear(2030);
        dto.setStart(start);
        dto.setEnd(end);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/budgets")
                .withAuth()
                .withBody(dto)
                .build();

        ct.post(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertNotNull(ctx.body);
        assertNotNull(bs.b);
        var b = (Budget) ctx.body;
        assertEquals("descr", b.getDescription());
        assertEquals(1, b.getCurrency());
        assertEquals(151.0, b.getValue());
        assertEquals(start, b.getStart());
        assertEquals(end, b.getEnd());
        assertEquals(15, b.getOwner());
        assertEquals(b, bs.b);
    }

    @Test
    public void testPostUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new BudgetController(jwt, null, null, null, null, TELEMETRY);
        var dto = new BudgetDto();
        dto.setValue(15.0);
        dto.setCurrency(1);
        var start = new Date();
        var end = (Date) start.clone();
        end.setYear(2030);
        dto.setStart(start);
        dto.setEnd(end);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/budgets")
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
        var b = new Budget();
        b.setOwner(15);
        b.setDescription("d");
        b.setCurrency(1);
        b.setValue(5.0);
        b.setId(1014);
        var bs = new RepositoryImpl<Budget>() {
            Budget upd;

            @Override
            public void update(String role, Budget model) {
                upd = model;
            }

            @Override
            public Budget get(String role, int key) {
                if (key == 1014) {
                    return b;
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
        var ct = new BudgetController(jwt, null, users, bs, curs, TELEMETRY);
        var dto = new BudgetDto();
        dto.setDescription("descr");
        dto.setCurrency(2);
        dto.setValue(15.0);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.PATCH)
                .withURI("/budgets/1024")
                .withPath("id", 1014)
                .withAuth()
                .withBody(dto)
                .build();

        ct.update(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(b, bs.upd);
        assertEquals(1014, b.getId());
        assertEquals("descr", b.getDescription());
        assertEquals(2, b.getCurrency());
        assertEquals(15, b.getValue());
    }

    @Test
    public void testUpdateUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new BudgetController(jwt, null, null, null, null, TELEMETRY);
        var dto = new BudgetDto();
        dto.setDescription("1");
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.PATCH)
                .withURI("/budgets/1")
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
        var b = new Budget();
        b.setId(13);
        b.setOwner(15);
        var bs = new RepositoryImpl<Budget>() {
            int id;

            @Override
            public Budget get(String role, int key) {
                if (key == 13) {
                    return b;
                }
                return null;
            }

            @Override
            public void delete(String role, int key) {
                id = key;
            }
        };
        var ct = new BudgetController(jwt, null, users, bs, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.DELETE)
                .withURI("/budgets/13")
                .withAuth()
                .withPath("id", 13)
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(13, bs.id);
    }

    @Test
    public void testDeleteUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new BudgetController(jwt, null, null, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.DELETE)
                .withURI("/budgets/1")
                .withPath("id", 1)
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }
}
