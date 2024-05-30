package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Goal;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;

@JavalinController("/goal")
public final class GoalController extends AuthBase {
    private final Repository<Goal> goals;

    public GoalController(JwtProvider<JwtUser> provider, Repository<User> users, Repository<Goal> goals) {
        super(provider, users);
        this.goals = goals;
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {

    }

    @Route(method = HandlerType.PUT)
    public void put(Context ctx) {

    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {

    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {

    }
}
