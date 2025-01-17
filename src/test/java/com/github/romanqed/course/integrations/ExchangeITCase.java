package com.github.romanqed.course.integrations;

import com.github.romanqed.course.MockUtil;
import com.github.romanqed.course.controllers.AuthController;
import com.github.romanqed.course.controllers.CurrencyController;
import com.github.romanqed.course.controllers.ExchangeController;
import com.github.romanqed.course.dto.ExchangeDto;
import com.github.romanqed.course.dto.Token;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.Exchange;
import com.github.romanqed.course.postgres.PostgresRepository;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class ExchangeITCase {
    private static String database;
    private static Connection connection;
    private static AuthController auth;
    private static CurrencyController currencies;
    private static ExchangeController exchanges;

    @BeforeAll
    public static void init() throws Throwable {
        database = Util.getRandomDb("exc");
        connection = Util.initDatabase(database, List.of(Util.TABLES, Util.ROLES));
        var curRepo = new PostgresRepository<>(
                connection,
                "currencies",
                Currency::new,
                (setter, obj) -> Currency.to(setter, (Currency) obj),
                (getter, obj) -> Currency.from(getter, (Currency) obj)
        );
        var exRepo = new PostgresRepository<>(
                connection,
                "exchanges",
                Exchange::new,
                (setter, obj) -> Exchange.to(setter, (Exchange) obj),
                (getter, obj) -> Exchange.from(getter, (Exchange) obj)
        );
        var jwt = Util.createJwtProvider();
        var encoder = Util.createEncoder();
        var userRepo = Util.initUserRepo(connection, encoder);
        auth = new AuthController(userRepo, jwt, encoder);
        currencies = new CurrencyController(jwt, userRepo, curRepo);
        exchanges = new ExchangeController(jwt, userRepo, exRepo, curRepo);
    }

    private static void assertExchanges(List<Exchange> es, Currency first, Currency second) {
        var map = new HashMap<Integer, Exchange>();
        map.put(es.get(0).getFrom(), es.get(0));
        map.put(es.get(1).getFrom(), es.get(1));
        assertEquals(2, map.size());
        var p1 = map.get(first.getId());
        var p2 = map.get(second.getId());
        assertNotNull(p1);
        assertNotNull(p2);
        assertEquals(second.getId(), p1.getTo());
        assertEquals(first.getId(), p2.getTo());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test() throws SQLException {
        // Login as admin
        var ctx = MockUtil.ctxBuilder()
                .withBody(Util.ofCreds("admin", "pass"))
                .build();
        auth.login(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var token = ((Token) ctx.body).getToken();
        // Add first currency
        ctx = MockUtil.ctxBuilder()
                .withBody(Util.ofName("first"))
                .withAuth(token)
                .build();
        currencies.post(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var first = (Currency) ctx.body;
        assertEquals("first", first.getName());
        // Add second currency
        ctx = MockUtil.ctxBuilder()
                .withBody(Util.ofName("second"))
                .withAuth(token)
                .build();
        currencies.post(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var second = (Currency) ctx.body;
        assertEquals("second", second.getName());
        // Add exchange
        var dto = new ExchangeDto();
        dto.setFrom(first.getId());
        dto.setTo(second.getId());
        dto.setFactor(0.01);
        ctx = MockUtil.ctxBuilder()
                .withBody(dto)
                .withAuth(token)
                .build();
        exchanges.post(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var posted = (List<Exchange>) ctx.body;
        // Get entries by controller
        ctx = MockUtil.ctxBuilder()
                .withEmptyQuery("from", Integer.class)
                .withEmptyQuery("to", Integer.class)
                .build();
        exchanges.find(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var found = (List<Exchange>) ctx.body;
        // Check if there is 2 exchange entries and they are valid
        assertEquals(2, posted.size());
        assertEquals(2, found.size());
        assertExchanges(posted, first, second);
        assertExchanges(found, first, second);
        // Delete exchange
        ctx = MockUtil.ctxBuilder()
                .withAuth(token)
                .withPath("id", posted.get(0).getId())
                .build();
        exchanges.delete(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        // Check if there is no exchange entries
        ctx = MockUtil.ctxBuilder()
                .withEmptyQuery("from", Integer.class)
                .withEmptyQuery("to", Integer.class)
                .build();
        exchanges.find(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        assertTrue(((List<Exchange>) ctx.body).isEmpty());
    }

    @AfterAll
    public static void destroy() throws SQLException {
        connection.close();
        Util.dropDatabase(database);
    }
}
