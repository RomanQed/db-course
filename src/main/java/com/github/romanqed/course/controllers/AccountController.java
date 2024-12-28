package com.github.romanqed.course.controllers;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.dto.AccountDto;
import com.github.romanqed.course.dto.DtoUtil;
import com.github.romanqed.course.javalin.JavalinController;
import com.github.romanqed.course.javalin.Route;
import com.github.romanqed.course.jwt.JwtProvider;
import com.github.romanqed.course.jwt.JwtUser;
import com.github.romanqed.course.models.Account;
import com.github.romanqed.course.models.Currency;
import com.github.romanqed.course.models.User;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;

import java.util.Objects;

@JavalinController("/account")
public final class AccountController extends AuthBase {
    private final Repository<Account> accounts;
    private final Repository<Currency> currencies;

    public AccountController(JwtProvider<JwtUser> provider,
                             Repository<User> users,
                             Repository<Account> accounts,
                             Repository<Currency> currencies) {
        super(provider, users);
        this.accounts = accounts;
        this.currencies = currencies;
    }

    @Route(method = HandlerType.GET, route = "/{id}")
    public void get(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var found = Util.seeOwned(ctx, this, accounts, id);
        if (found == null) {
            return;
        }
        ctx.json(found);
    }

    @Route(method = HandlerType.POST)
    public void post(Context ctx) {
        var dto = DtoUtil.validate(ctx, AccountDto.class);
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
        var account = Account.of(user.getId(), currency, dto.getValue());
        account.setDescription(Objects.requireNonNullElse(dto.getDescription(), ""));
        accounts.put(USER_ROLE, account);
        ctx.json(account);
    }

    @Route(method = HandlerType.PATCH, route = "/{id}")
    public void update(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var dto = DtoUtil.parse(ctx, AccountDto.class);
        if (dto == null) {
            return;
        }
        var currency = dto.getCurrency();
        var description = dto.getDescription();
        if (currency == null && description == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            return;
        }
        var account = Util.seeOwned(ctx, this, accounts, id);
        if (account == null) {
            return;
        }
        if (currency != null) {
            if (!currencies.exists(USER_ROLE, currency)) {
                ctx.status(HttpStatus.NOT_FOUND);
                return;
            }
            account.setCurrency(currency);
        }
        if (description != null) {
            account.setDescription(description);
        }
        accounts.update(USER_ROLE, account);
    }

    @Route(method = HandlerType.DELETE, route = "/{id}")
    public void delete(Context ctx) {
        var id = ctx.pathParamAsClass("id", Integer.class).get();
        var account = Util.seeOwned(ctx, this, accounts, id);
        if (account == null) {
            return;
        }
        accounts.delete(USER_ROLE, account.getId());
    }
}
