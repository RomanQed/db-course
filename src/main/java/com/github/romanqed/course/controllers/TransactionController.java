package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Transaction;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;

@JavalinController("/transaction")
public final class TransactionController extends AuthBase {
    private final Repository<Transaction> transactions;

    public TransactionController(JwtProvider<JwtUser> provider,
                                 Repository<User> users,
                                 Repository<Transaction> transactions) {
        super(provider, users);
        this.transactions = transactions;
    }

    public void get(Context ctx) {

    }

    public void put(Context ctx) {

    }

    public void delete(Context ctx) {

    }
}
