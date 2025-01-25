package com.github.romanqed.course.units;

import com.github.romanqed.course.MockUtil;
import com.github.romanqed.course.controllers.TransactionController;
import com.github.romanqed.course.dto.TransactionDto;
import com.github.romanqed.course.models.Account;
import com.github.romanqed.course.models.Category;
import com.github.romanqed.course.models.Transaction;
import com.github.romanqed.course.models.User;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.github.romanqed.course.units.Otel.TELEMETRY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TransactionControllerTest {

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
        var tr = new Transaction();
        tr.setOwner(15);
        tr.setId(1);
        var trs = new RepositoryImpl<Transaction>() {
            @Override
            public Transaction get(String role, int key) {
                return tr;
            }
        };
        var ct = new TransactionController(jwt, null, users, trs, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/transactions/1")
                .withAuth()
                .withPath("id", 1)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(tr, ctx.body);
    }

    @Test
    public void testGetUnauthorized() {
        var jwt = MockUtil.mockProvider(0);
        var ct = new TransactionController(
                jwt,
                null,
                null,
                null,
                null,
                null,
                TELEMETRY
        );
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/transactions/1")
                .withPath("id", 1)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testPost() throws SQLException {
        var jwt = MockUtil.mockProvider(15);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setId(key);
                return ret;
            }
        };
        var cats = new RepositoryImpl<Category>() {
            @Override
            public boolean exists(String role, int id) {
                return id == 15;
            }
        };
        var acs = new RepositoryImpl<Account>() {
            @Override
            public boolean exists(String role, int id) {
                return id == 16 || id == 17;
            }
        };
        var trs = new RepositoryImpl<Transaction>();
        //
        var conn = Mockito.mock(Connection.class);
        var st = Mockito.mock(Statement.class);
        var rs = new ResultSetImpl() {
            @Override
            public boolean next() {
                return true;
            }

            @Override
            public int getInt(int columnIndex) {
                return 5;
            }
        };
        Mockito
                .when(st.execute(Mockito.anyString()))
                .thenReturn(true);
        Mockito
                .when(st.getResultSet())
                .thenReturn(rs);
        Mockito.when(conn.createStatement())
                .thenReturn(st);
        //
        var ct = new TransactionController(jwt, conn, users, trs, cats, acs, TELEMETRY);
        var dto = new TransactionDto();
        dto.setFrom(16);
        dto.setTo(17);
        dto.setDescription("descr");
        dto.setCategory(15);
        dto.setValue(10.0);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/transactions")
                .withAuth()
                .withBody(dto)
                .build();

        ct.post(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        var t = (Transaction) ctx.body;
        assertEquals(16, t.getFrom());
        assertEquals(17, t.getTo());
        assertEquals("descr", t.getDescription());
        assertEquals(15, t.getCategory());
        assertEquals(10.0, t.getValue());
        assertEquals(5, t.getId());
    }

    @Test
    public void postNotAuthorized() throws SQLException {
        var jwt = MockUtil.mockProvider(0);
        var ct = new TransactionController(
                jwt,
                null,
                null,
                null,
                null,
                null,
                TELEMETRY
        );
        var dto = new TransactionDto();
        dto.setFrom(16);
        dto.setTo(17);
        dto.setDescription("descr");
        dto.setCategory(15);
        dto.setValue(10.0);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/transactions")
                .withBody(dto)
                .build();

        ct.post(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }

    @Test
    public void testDelete() throws SQLException {
        var jwt = MockUtil.mockProvider(15);
        var users = new RepositoryImpl<User>() {
            @Override
            public User get(String role, int key) {
                var ret = new User();
                ret.setId(key);
                return ret;
            }
        };
        var trs = new RepositoryImpl<Transaction>() {
            @Override
            public Transaction get(String role, int key) {
                var ret = new Transaction();
                ret.setId(key);
                ret.setOwner(15);
                return ret;
            }
        };
        //
        var conn = Mockito.mock(Connection.class);
        var st = Mockito.mock(Statement.class);
        Mockito
                .when(st.executeUpdate(Mockito.anyString()))
                .thenReturn(1);
        Mockito.when(conn.createStatement())
                .thenReturn(st);
        //
        var ct = new TransactionController(jwt, conn, users, trs, null, null, TELEMETRY);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.DELETE)
                .withURI("/transactions/1")
                .withAuth()
                .withPath("id", 1)
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
    }

    @Test
    public void testDeleteNotAuthorized() throws SQLException {
        var jwt = MockUtil.mockProvider(0);
        var ct = new TransactionController(
                jwt,
                null,
                null,
                null,
                null,
                null,
                TELEMETRY
        );
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.DELETE)
                .withURI("/transactions/1")
                .withPath("id", 1)
                .build();

        ct.delete(ctx.mock);

        assertEquals(HttpStatus.UNAUTHORIZED, ctx.status);
    }
}
