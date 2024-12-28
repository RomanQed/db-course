package com.github.romanqed.course;

import com.github.romanqed.course.controllers.CurrencyController;
import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.User;
import com.github.romanqed.course.postgres.DbRepo;
import io.javalin.http.HttpStatus;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ClassicCurrencyControllerTest {
    static Connection connection;
    static JdbcDataSource ds = new JdbcDataSource();

    @BeforeAll
    public static void initDb() throws Throwable {
        ds.setURL("jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("sa");
        connection = Util.initDatabase(ds, List.of(Util.TABLES));
    }

    public static Repository<User> newUserRepo() {
        return new DbRepo<>(connection, "users", User::new, (s, u) -> {
            User.to(s, (User) u);
        }, (g, u) -> {
            User.from(g, (User) u);
        });
    }

    public static Repository<Currency> newCurRepo() {
        return new DbRepo<>(connection, "currencies", Currency::new, (s, u) -> {
            Currency.to(s, (Currency) u);
        }, (g, u) -> {
            Currency.from(g, (Currency) u);
        });
    }

    @Test
    public void testGet() {
        var curs = newCurRepo();
        var cur = new Currency();
        cur.setName("4");
        curs.put("", cur);
        var ct = new CurrencyController(null, null, curs);
        var ctx = MockUtil.ctxBuilder()
                .withPath("id", 1)
                .build();

        ct.get(ctx.mock);

        assertEquals(HttpStatus.OK, ctx.status);
        assertEquals(1, ((Currency) ctx.body).getId());
    }

//    @Test
//    public void testGetNotExisting() {
////        var repo = new DbRepo();
//        var ct = new CurrencyController(null, null, curs);
//        var ctx = MockUtil.ctxBuilder()
//                .withPath("id", 10)
//                .build();
//
//        ct.get(ctx.mock);
//
//        assertEquals(HttpStatus.NOT_FOUND, ctx.status);
//    }
//
//    @Test
//    public void testFind() {
//        var lst = new ArrayList<Currency>();
//        var curs = new RepositoryImpl<Currency>() {
//            @Override
//            public List<Currency> get(String role) {
//                return lst;
//            }
//        };
//        var ct = new CurrencyController(null, null, curs);
//        var ctx = MockUtil.mockCtx();
//
//        ct.find(ctx.mock);
//
//        assertEquals(HttpStatus.OK, ctx.status);
//        assertEquals(lst, ctx.body);
//    }
//
//    @Test
//    public void testPost() {
//        var jwt = MockUtil.mockProvider(0);
//        var users = new RepositoryImpl<User>() {
//            @Override
//            public User get(String role, int key) {
//                var ret = new User();
//                ret.setAdmin(true);
//                return ret;
//            }
//        };
//        var cs = new RepositoryImpl<Currency>() {
//            Currency c;
//            @Override
//            public void put(String role, Currency model) {
//                c = model;
//            }
//        };
//        var ct = new CurrencyController(jwt, users, cs);
//        var dto = new NameDto();
//        dto.setName("tcur1");
//        var ctx = MockUtil.ctxBuilder()
//                .withBody(dto)
//                .withAuth()
//                .build();
//
//        ct.post(ctx.mock);
//
//        assertEquals(HttpStatus.OK, ctx.status);
//        assertEquals("tcur1", cs.c.getName());
//    }
//
//    @Test
//    public void testPostAsNotAdmin() {
//        var jwt = MockUtil.mockProvider(0);
//        var users = new RepositoryImpl<User>() {
//            @Override
//            public User get(String role, int key) {
//                return new User();
//            }
//        };
//        var cs = new RepositoryImpl<Currency>();
//        var ct = new CurrencyController(jwt, users, cs);
//        var dto = new NameDto();
//        dto.setName("tcur1");
//        var ctx = MockUtil.ctxBuilder()
//                .withBody(dto)
//                .withAuth()
//                .build();
//
//        ct.post(ctx.mock);
//
//        assertEquals(HttpStatus.FORBIDDEN, ctx.status);
//    }
//
//    @Test
//    public void testUpdate() {
//        var jwt = MockUtil.mockProvider(0);
//        var users = new RepositoryImpl<User>() {
//            @Override
//            public User get(String role, int key) {
//                var ret = new User();
//                ret.setAdmin(true);
//                return ret;
//            }
//        };
//        var cs = new RepositoryImpl<Currency>() {
//            Currency c;
//            @Override
//            public Currency get(String role, int key) {
//                var ret = new Currency();
//                ret.setId(key);
//                return ret;
//            }
//
//            @Override
//            public void update(String role, Currency model) {
//                c = model;
//            }
//        };
//        var ct = new CurrencyController(jwt, users, cs);
//        var dto = new NameDto();
//        dto.setName("tc2");
//        var ctx = MockUtil.ctxBuilder()
//                .withPath("id", 13)
//                .withBody(dto)
//                .withAuth()
//                .build();
//
//        ct.update(ctx.mock);
//
//        assertEquals(HttpStatus.OK, ctx.status);
//        assertEquals(13, cs.c.getId());
//        assertEquals("tc2", cs.c.getName());
//    }
//
//    @Test
//    public void testUpdateAsNotAdmin() {
//        var jwt = MockUtil.mockProvider(0);
//        var users = new RepositoryImpl<User>() {
//            @Override
//            public User get(String role, int key) {
//                return new User();
//            }
//        };
//        var cs = new RepositoryImpl<Currency>();
//        var ct = new CurrencyController(jwt, users, cs);
//        var dto = new NameDto();
//        dto.setName("tc1");
//        var ctx = MockUtil.ctxBuilder()
//                .withPath("id", 13)
//                .withBody(dto)
//                .withAuth()
//                .build();
//
//        ct.update(ctx.mock);
//
//        assertEquals(HttpStatus.FORBIDDEN, ctx.status);
//    }
//
//    @Test
//    public void testDelete() {
//        var jwt = MockUtil.mockProvider(0);
//        var users = new RepositoryImpl<User>() {
//            @Override
//            public User get(String role, int key) {
//                var ret = new User();
//                ret.setAdmin(true);
//                return ret;
//            }
//        };
//        var cs = new RepositoryImpl<Currency>() {
//            int id;
//
//            @Override
//            public void delete(String role, int key) {
//                id = key;
//            }
//
//            @Override
//            public boolean exists(String role, int id) {
//                return id == 14;
//            }
//        };
//        var ct = new CurrencyController(jwt, users, cs);
//        var ctx = MockUtil.ctxBuilder()
//                .withPath("id", 14)
//                .withAuth()
//                .build();
//
//        ct.delete(ctx.mock);
//
//        assertEquals(HttpStatus.OK, ctx.status);
//        assertEquals(14, cs.id);
//    }
//
//    @Test
//    public void testDeleteAsNotAdmin() {
//        var jwt = MockUtil.mockProvider(0);
//        var users = new RepositoryImpl<User>() {
//            @Override
//            public User get(String role, int key) {
//                return new User();
//            }
//        };
//        var cs = new RepositoryImpl<Currency>();
//        var ct = new CurrencyController(jwt, users, cs);
//        var ctx = MockUtil.ctxBuilder()
//                .withPath("id", 14)
//                .withAuth()
//                .build();
//
//        ct.delete(ctx.mock);
//
//        assertEquals(HttpStatus.FORBIDDEN, ctx.status);
//    }
}
