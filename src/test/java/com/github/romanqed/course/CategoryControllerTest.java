package com.github.romanqed.course;

import com.github.romanqed.course.controllers.CategoryController;
import com.github.romanqed.course.models.Category;
import com.github.romanqed.course.models.Transaction;
import com.github.romanqed.course.models.User;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO
public final class CategoryControllerTest {

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
        var ct = new CategoryController(jwt, users, cats, trs);
        var ctx = MockUtil.ctxBuilder()
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
    public void testGet() {

    }

    @Test
    public void testFind() {

    }

    @Test
    public void testPost() {

    }

    @Test
    public void testUpdate() {

    }

    @Test
    public void testDelete() {

    }
}
