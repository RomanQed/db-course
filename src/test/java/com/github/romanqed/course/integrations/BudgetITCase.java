package com.github.romanqed.course.integrations;

import com.github.romanqed.course.MockUtil;
import com.github.romanqed.course.TimeUtil;
import com.github.romanqed.course.controllers.AuthController;
import com.github.romanqed.course.controllers.BudgetController;
import com.github.romanqed.course.controllers.TransactionController;
import com.github.romanqed.course.dto.BudgetDto;
import com.github.romanqed.course.dto.BudgetStatus;
import com.github.romanqed.course.dto.Token;
import com.github.romanqed.course.dto.TransactionDto;
import com.github.romanqed.course.models.*;
import com.github.romanqed.course.otel.OtelUtil;
import com.github.romanqed.course.postgres.PostgresRepository;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class BudgetITCase {
    private static String database;
    private static Connection connection;
    private static int curId;
    private static int catId;
    private static int accId;
    private static AuthController auth;
    private static TransactionController transactions;
    private static BudgetController budgets;

    @BeforeAll
    public static void init() throws Throwable {
        database = Util.getRandomDb("bdg");
        connection = Util.initDatabase(database, List.of(
                Util.TABLES,
                Util.ROLES,
                Util.BUDGETS,
                Util.TRANSACTIONS
        ));
        // Init currencies
        var curRepo = new PostgresRepository<>(
                connection,
                "currencies",
                Currency::new,
                (setter, obj) -> Currency.to(setter, (Currency) obj),
                (getter, obj) -> Currency.from(getter, (Currency) obj)
        );
        var cur = new Currency();
        cur.setName("test_cur");
        curRepo.put(Util.SYSTEM_ROLE, cur);
        curId = cur.getId();
        // Init categories
        var catRepo = new PostgresRepository<>(
                connection,
                "categories",
                Category::new,
                (setter, obj) -> Category.to(setter, (Category) obj),
                (getter, obj) -> Category.from(getter, (Category) obj)
        );
        var cat = new Category();
        cat.setName("test_cat");
        catRepo.put(Util.SYSTEM_ROLE, cat);
        catId = cat.getId();
        // Init users
        var encoder = Util.createEncoder();
        var userRepo = Util.initUserRepo(connection, encoder);
        var user = userRepo.get(Util.SYSTEM_ROLE).get(0);
        // Init accounts
        var accRepo = new PostgresRepository<>(
                connection,
                "accounts",
                Account::new,
                (setter, obj) -> Account.to(setter, (Account) obj),
                (getter, obj) -> Account.from(getter, (Account) obj)
        );
        var account = new Account();
        account.setOwner(user.getId());
        account.setCurrency(curId);
        account.setValue(0);
        account.setDescription("descr");
        accRepo.put(Util.SYSTEM_ROLE, account);
        accId = account.getId();
        // Init transactions
        var trRepo = new PostgresRepository<>(
                connection,
                "transactions",
                Transaction::new,
                (setter, obj) -> Transaction.to(setter, (Transaction) obj),
                (getter, obj) -> Transaction.from(getter, (Transaction) obj)
        );
        // Init budgets
        var bdRepo = new PostgresRepository<>(
                connection,
                "budgets",
                Budget::new,
                (setter, obj) -> Budget.to(setter, (Budget) obj),
                (getter, obj) -> Budget.from(getter, (Budget) obj)
        );
        // Init controllers
        var jwt = Util.createJwtProvider();
        var telemetry = OtelUtil.createOtel("BudgetITCase");
        auth = new AuthController(userRepo, jwt, encoder, null, telemetry);
        transactions = new TransactionController(
                jwt,
                connection,
                userRepo,
                trRepo,
                catRepo,
                accRepo,
                telemetry
        );
        budgets = new BudgetController(
                jwt,
                connection,
                userRepo,
                bdRepo,
                curRepo,
                telemetry
        );
    }

    private static void makeTransaction(String token, double value, Integer from, Integer to) throws SQLException {
        var dto = new TransactionDto();
        dto.setCategory(catId);
        dto.setFrom(from);
        dto.setTo(to);
        dto.setValue(value);
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/transactions")
                .withAuth(token)
                .withBody(dto)
                .build();
        transactions.post(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
    }

    @AfterAll
    public static void destroy() throws SQLException {
        connection.close();
        Util.dropDatabase(database);
    }

    @Test
    public void test() throws SQLException {
        // Login
        var ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/login")
                .withBody(Util.ofCreds("admin", "pass"))
                .build();
        auth.login(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var token = ((Token) ctx.body).getToken();
        // Create budget
        var bDto = new BudgetDto();
        bDto.setCurrency(curId);
        bDto.setStart(TimeUtil.ofYear(1990));
        bDto.setEnd(TimeUtil.ofYear(2100));
        bDto.setDescription("descr");
        bDto.setValue(0.0);
        ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.POST)
                .withURI("/budgets")
                .withAuth(token)
                .withBody(bDto)
                .build();
        budgets.post(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var budget = (Budget) ctx.body;
        assertEquals("descr", budget.getDescription());
        // Make transactions
        // -100
        // +1000
        makeTransaction(token, 100, accId, null);
        makeTransaction(token, 1000, null, accId);
        // Calculate budget
        ctx = MockUtil.ctxBuilder()
                .withMethod(HandlerType.GET)
                .withURI("/budgets/" + budget.getId() + "/status")
                .withAuth(token)
                .withPath("id", budget.getId())
                .build();
        budgets.status(ctx.mock);
        assertEquals(HttpStatus.OK, ctx.status);
        var status = (BudgetStatus) ctx.body;
        assertEquals(100, status.getSpent());
        assertEquals(1000, status.getGot());
        assertEquals(900, status.getTotal());
    }
}
