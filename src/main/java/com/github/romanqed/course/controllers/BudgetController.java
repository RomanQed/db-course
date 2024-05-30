package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.BudgetDto;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Budget;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

import java.util.Objects;

@JavalinController("/budget")
public final class BudgetController extends AuthBase {
    private final Repository<Budget> budgets;
    private final Repository<Currency> currencies;

    public BudgetController(JwtProvider<JwtUser> provider,
                            Repository<User> users,
                            Repository<Budget> budgets,
                            Repository<Currency> currencies) {
        super(provider, users);
        this.budgets = budgets;
        this.currencies = currencies;
    }

    // TODO Implement status or calc

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = Util.seeOwned(ctx, this, budgets, id);
        if (found == null) {
            return;
        }
        ctx.json(found);
    }

    @Route(method = HandlerType.PUT)
    public void put(Context ctx) {
        var dto = DtoUtil.validate(ctx, BudgetDto.class);
        if (dto == null) {
            return;
        }
        var user = getCheckedUser(ctx);
        if (user == null) {
            return;
        }
        var currency = dto.getCurrency();
        if (!currencies.exists(USER_ROLE, currency)) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        var budget = Budget.of(user.getId(), currency, dto.getValue());
        budget.setStart(dto.getStart());
        budget.setEnd(dto.getEnd());
        budget.setDescription(Objects.requireNonNullElse(dto.getDescription(), ""));
        budgets.put(USER_ROLE, budget);
        ctx.json(budget);
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.parse(ctx, BudgetDto.class);
        if (dto == null) {
            return;
        }
        var currency = dto.getCurrency();
        var description = dto.getDescription();
        var value = dto.getValue();
        if (currency == null && description == null && value == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        if (currency != null && !currencies.exists(USER_ROLE, currency)) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        if (value != null && value < 1) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }
        var budget = Util.seeOwned(ctx, this, budgets, id);
        if (budget == null) {
            return;
        }
        if (currency != null) {
            budget.setCurrency(currency);
        }
        if (description != null) {
            budget.setDescription(description);
        }
        if (value != null) {
            budget.setValue(value);
        }
        budgets.update(USER_ROLE, budget);
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var budget = Util.seeOwned(ctx, this, budgets, id);
        if (budget == null) {
            return;
        }
        budgets.delete(USER_ROLE, budget.getId());
    }
}
