package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.dto.GoalDto;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Account;
import com.github.romanqed.course.models.Goal;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

import java.util.Objects;

@JavalinController("/goal")
public final class GoalController extends AuthBase {
    private final Repository<Goal> goals;
    private final Repository<Account> accounts;

    public GoalController(JwtProvider<JwtUser> provider,
                          Repository<User> users,
                          Repository<Goal> goals,
                          Repository<Account> accounts) {
        super(provider, users);
        this.goals = goals;
        this.accounts = accounts;
    }

    // TODO Implement /status or maybe /calc route

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = Util.seeOwned(ctx, this, goals, id);
        if (found == null) {
            return;
        }
        ctx.json(found);
    }

    @Route(method = HandlerType.PUT)
    public void put(Context ctx) {
        var dto = DtoUtil.validate(ctx, GoalDto.class);
        if (dto == null) {
            return;
        }
        var user = getCheckedUser(ctx);
        if (user == null) {
            return;
        }
        var account = dto.getAccount();
        if (!accounts.exists(USER_ROLE, account)) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        var goal = Goal.of(user.getId(), account, dto.getTarget());
        goal.setDescription(Objects.requireNonNullElse(dto.getDescription(), ""));
        goals.put(USER_ROLE, goal);
        ctx.json(goal);
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.parse(ctx, GoalDto.class);
        if (dto == null) {
            return;
        }
        var account = dto.getAccount();
        var target = dto.getTarget();
        var description = dto.getDescription();
        if (account == null && target == null && description == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        if (target != null && target < 1) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        if (account != null && !accounts.exists(USER_ROLE, account)) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        var goal = Util.seeOwned(ctx, this, goals, id);
        if (goal == null) {
            return;
        }
        if (account != null) {
            goal.setAccount(account);
        }
        if (target != null) {
            goal.setTarget(target);
        }
        if (description != null) {
            goal.setDescription(description);
        }
        goals.update(USER_ROLE, goal);
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var goal = Util.seeOwned(ctx, this, goals, id);
        if (goal == null) {
            return;
        }
        goals.delete(USER_ROLE, goal.getId());
    }
}
