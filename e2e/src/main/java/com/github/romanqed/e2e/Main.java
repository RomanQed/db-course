package com.github.romanqed.e2e;

import com.github.romanqed.course.database.Database;
import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.di.ScanProviderDirector;
import com.github.romanqed.course.dto.*;
import com.github.romanqed.course.hash.Encoder;
import com.github.romanqed.course.models.*;
import com.github.romanqed.jfunc.Runnable0;
import com.github.romanqed.jtype.Types;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.amayaframework.di.Builders;
import io.github.amayaframework.di.HashRepository;
import io.github.amayaframework.di.ServiceProvider;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;

public class Main {
    private static final String SYSTEM_ROLE = "_service";
    private static final Gson GSON = new Gson();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void main(String[] args) throws Throwable {
        // Run app
        var holder = new int[1];
        System.out.println("Starting app...");
        var shutdown = createApp(holder);
        var port = holder[0];
        // Prepare http client and base uri
        var uri = URI.create("http://localhost:" + port + "/");
        System.out.println("App started, root: " + uri);
        // Test app
        try {
            System.out.println("Testing app...");
            testApp(uri);
            System.out.println("Success");
        } finally {
            System.out.println("Stopping app...");
            // Shutdown app
            shutdown.run();
        }
    }

    @SuppressWarnings("unchecked")
    private static void testApp(URI root) throws IOException, InterruptedException {
        // E2E scenario
        // 1. Preparation
        // 1.1 Login as admin
        // 1.2-1.4 Create categories, currencies, exchanges
        // 2. Demo
        // 2.1 Register new user
        // 2.2 Create account
        // 2.3 Create budget
        // 2.4 Create transactions
        // 2.5 See user's accounts, budgets and transactions
        // 2.6 Calculate budget

        // === 1. Preparation
        // 1.1 Login as admin
        System.out.println("[1.1] Login as admin");
        var r1 = HttpRequest.newBuilder()
                .uri(root.resolve("/login"))
                .POST(of("admin", "pass"))
                .build();
        var token = from(send(r1), Token.class).getToken();
        System.out.println("[1.1] Token: " + token);
        // 1.2 Create simple category
        System.out.println("[1.2] Create category TestCat");
        var r2 = HttpRequest.newBuilder()
                .uri(root.resolve("/categories"))
                .header("Authorization", "Bearer " + token)
                .POST(of("TestCat"))
                .build();
        var category = from(send(r2), Category.class);
        System.out.println("[1.2] Category id: " + category.getId());
        // 1.3 Create simple currencies
        var c1 = createCurrency(root, token, "TestCur1"); // usd
        var c2 = createCurrency(root, token, "TestCur2"); // rub
        // 1.4 Create exchange
        System.out.println("[1.4] Create exchanges between TestCur1 and TestCur2, 1 TC1 = 100 TC2");
        var eDto = new ExchangeDto();
        eDto.setFrom(c1);
        eDto.setTo(c2);
        eDto.setFactor(100.0);
        var r3 = HttpRequest.newBuilder()
                .uri(root.resolve("/exchanges"))
                .header("Authorization", "Bearer " + token)
                .POST(to(eDto))
                .build();
        var exchanges = (List<Exchange>) from(send(r3), new TypeToken<List<Exchange>>(){}.getType());
        System.out.print("[1.4] Exchanges ids: ");
        System.out.println(exchanges.get(0).getId() + ", " + exchanges.get(1).getId());

        // === 2. Demo
        // 2.1 Register new user
        System.out.println("[2.1] Register a new user 'user'");
        var r4 = HttpRequest.newBuilder()
                .uri(root.resolve("/register"))
                .POST(of("user", "mypass"))
                .build();
        token = from(send(r4), Token.class).getToken();
        System.out.println("[2.1] Token: " + token);
        // 2.2 Create account
        System.out.println(
                "[2.2] Create account 'TestAcc' with value 0, currency 'TestCur1' and description 'D:TestAcc'"
        );
        var aDto = new AccountDto();
        aDto.setValue(0d);
        aDto.setCurrency(c1);
        aDto.setDescription("D:TestAcc");
        var r5 = HttpRequest.newBuilder()
                .uri(root.resolve("/accounts"))
                .header("Authorization", "Bearer " + token)
                .POST(to(aDto))
                .build();
        var account = from(send(r5), Account.class);
        System.out.println("[2.2] Account id: " + account.getId());
        // 2.3 Create budget
        System.out.println(
                "[2.3] Create empty budget 'TestBdg' for 1999-2100 in currency TestCur2 with description 'D:TestBdg'"
        );
        var bDto = new BudgetDto();
        bDto.setValue(0d);
        bDto.setStart(TimeUtil.ofYear(1999));
        bDto.setEnd(TimeUtil.ofYear(2100));
        bDto.setCurrency(c2);
        bDto.setDescription("D:TestBdg");
        var r6 = HttpRequest.newBuilder()
                .uri(root.resolve("/budgets"))
                .header("Authorization", "Bearer " + token)
                .POST(to(bDto))
                .build();
        var budget = from(send(r6), Budget.class);
        System.out.println("[2.3] Budget id: " + budget.getId());
        // 2.4 Create transaction
        System.out.println("[2.4] Create couple of transaction");
        // Income transaction
        System.out.println("[2.4] Create income transaction with value 100");
        var t1Dto = new TransactionDto();
        t1Dto.setCategory(category.getId());
        t1Dto.setTo(account.getId());
        t1Dto.setValue(100d);
        var r7 = HttpRequest.newBuilder()
                .uri(root.resolve("/transactions"))
                .header("Authorization", "Bearer " + token)
                .POST(to(t1Dto))
                .build();
        var t1 = from(send(r7), Transaction.class);
        System.out.println("[2.4] Income transaction id: " + t1.getId());
        // Outcome transaction
        System.out.println("[2.4] Create outcome transaction with value 20");
        var t2Dto = new TransactionDto();
        t2Dto.setCategory(category.getId());
        t2Dto.setFrom(account.getId());
        t2Dto.setValue(20d);
        var r8 = HttpRequest.newBuilder()
                .uri(root.resolve("/transactions"))
                .header("Authorization", "Bearer " + token)
                .POST(to(t2Dto))
                .build();
        var t2 = from(send(r8), Transaction.class);
        System.out.println("[2.5] Outcome transaction id: " + t2.getId());
        // [2.5] See user's accounts, budgets and transactions
        System.out.println("[2.5] See user's accounts, budgets and transactions");
        // Get user
        System.out.println("[2.5] See user");
        var user = (User) get(root, "/users", token, User.class);
        print(user);
        var id = user.getId();
        // List user accounts
        System.out.println("[2.5] See accounts");
        var accounts = (List<Account>) get(
                root,
                "/users/" + id + "/accounts",
                token,
                new TypeToken<List<Account>>(){}.getType()
        );
        accounts.forEach(Main::print);
        if (accounts.size() != 1 || accounts.get(0).getId() != account.getId()) {
            throw new RuntimeException("Account check failed");
        }
        // List user budgets
        System.out.println("[2.5] See budgets");
        var budgets = (List<Budget>) get(
                root,
                "/users/" + id + "/budgets",
                token,
                new TypeToken<List<Budget>>(){}.getType()
        );
        budgets.forEach(Main::print);
        if (budgets.size() != 1 || budgets.get(0).getId() != budget.getId()) {
            throw new RuntimeException("Budget check failed");
        }
        // List user transactions
        System.out.println("[2.5] See transactions");
        var transactions = (List<Transaction>) get(
                root,
                "/users/" + id + "/transactions",
                token,
                new TypeToken<List<Transaction>>(){}.getType()
        );
        transactions.forEach(Main::print);
        var s1 = new HashSet<Integer>();
        var s2 = new HashSet<Integer>();
        s1.add(t1.getId());
        s1.add(t2.getId());
        s2.add(transactions.get(0).getId());
        s2.add(transactions.get(1).getId());
        if (transactions.size() != 2 || !s1.equals(s2)) {
            throw new RuntimeException("Transaction check failed");
        }
        // [2.6] Calculate budget
        // Account in TC1
        // +100; -20 => 80
        // Budget in TC2
        // +10000; -2000 => 8000
        System.out.println("[2.6] Get budget status");
        var status = (BudgetStatus) get(root, "/budgets/" + budget.getId() + "/status", token, BudgetStatus.class);
        System.out.println("[2.6] Total income: " + status.getGot());
        if (status.getGot() != 10000d) {
            throw new RuntimeException("Budget 'got' value corrupted");
        }
        System.out.println("[2.6] Total outcome: " + status.getSpent());
        if (status.getSpent() != 2000d) {
            throw new RuntimeException("Budget 'spent' value corrupted");
        }
        System.out.println("[2.6] Total: " + status.getTotal());
        if (status.getTotal() != 8000d) {
            throw new RuntimeException("Budget 'total' value corrupted");
        }
        System.out.println("[OK]");
    }

    private static void print(Transaction t) {
        System.out.println("Transaction " + t.getId());
        System.out.println("    Owner: " + t.getOwner());
        System.out.println("    Value: " + t.getValue());
        System.out.println("    Timestamp: " + t.getTimestamp());
        var from = t.getFrom();
        var to = t.getTo();
        if (from != null) {
            System.out.println("    From: " + from);
        }
        if (to != null) {
            System.out.println("    To: " + to);
        }
        System.out.println("    Category: " + t.getCategory());
    }

    private static void print(Budget budget) {
        System.out.println("Budget " + budget.getId());
        System.out.println("    Value: " + budget.getValue());
        System.out.println("    Owner: " + budget.getOwner());
        System.out.println("    Start: " + budget.getStart());
        System.out.println("    End: " + budget.getEnd());
        System.out.println("    Description: " + budget.getDescription());
        System.out.println("    Currency: " + budget.getCurrency());
    }

    private static void print(User user) {
        System.out.println("User " + user.getId());
        System.out.println("    Login: " + user.getLogin());
        System.out.println("    Password(hash): " + user.getPassword());
    }

    private static void print(Account account) {
        System.out.println("Account " + account.getId());
        System.out.println("    Value: " + account.getValue());
        System.out.println("    Owner: " + account.getOwner());
        System.out.println("    Description: " + account.getDescription());
        System.out.println("    Currency: " + account.getCurrency());
    }

    private static <T> T get(URI root, String uri, String token, Type type)
            throws IOException, InterruptedException {
        var builder = HttpRequest.newBuilder()
                .uri(root.resolve(uri))
                .GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        var request = builder.build();
        var rsp = send(request);
        return from(rsp, type);
    }

    private static int createCurrency(URI root, String token, String name) throws IOException, InterruptedException {
        System.out.println("[1.3] Create currency " + name);
        var req = HttpRequest.newBuilder()
                .uri(root.resolve("/currencies"))
                .header("Authorization", "Bearer " + token)
                .POST(of(name))
                .build();
        var rsp = from(send(req), Currency.class);
        var id = rsp.getId();
        System.out.println("[1.3] Currency " + name + " id: " + id);
        return id;
    }

    private static HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpRequest.BodyPublisher of(String login, String password) {
        var ret = new Credentials();
        ret.setLogin(login);
        ret.setPassword(password);
        return to(ret);
    }

    private static HttpRequest.BodyPublisher of(String name) {
        var ret = new NameDto();
        ret.setName(name);
        return to(ret);
    }

    private static HttpRequest.BodyPublisher to(Object obj) {
        var raw = GSON.toJson(obj);
        return HttpRequest.BodyPublishers.ofString(raw);
    }

    private static <T> T from(HttpResponse<String> response, Class<T> type) {
        var code = response.statusCode();
        if (code != 200) {
            throw new IllegalStateException("Unexpected response code: " + code);
        }
        var raw = response.body();
        return GSON.fromJson(raw, type);
    }

    private static <T> T from(HttpResponse<String> response, Type type) {
        var code = response.statusCode();
        if (code != 200) {
            throw new IllegalStateException("Unexpected response code: " + code);
        }
        var raw = response.body();
        return GSON.fromJson(raw, type);
    }

    private static Runnable0 createApp(int[] holder) {
        // Configure DI
        var repository = new HashRepository();
        var builder = Builders.createChecked();
        builder.setRepository(repository);
        // Stub project logger
        var logger = LoggerFactory.getLogger("main");
        builder.addService(Logger.class, () -> logger);
        // Process all di-dependent actions
        var director = new ScanProviderDirector();
        director.setBuilder(builder);
        var provider = director.build();
        // Get postgres db instance
        var postgres = provider.instantiate(Database.class);
        initAdminUser(provider);
        var javalin = provider.instantiate(Javalin.class);
        javalin.start(0);
        holder[0] = javalin.port();
        return () -> {
            javalin.stop();
            postgres.close();
            System.out.println("App stopped");
            System.exit(0);
        };
    }

    @SuppressWarnings("unchecked")
    private static void initAdminUser(ServiceProvider provider) {
        var encoder = provider.instantiate(Encoder.class);
        var users = (Repository<User>) provider.instantiate(Types.of(Repository.class, User.class));
        if (users.exists(SYSTEM_ROLE, "login", "admin")) {
            return;
        }
        var user = User.of("admin", encoder.encode("pass"));
        user.setAdmin(true);
        users.put(SYSTEM_ROLE, user);
    }
}
