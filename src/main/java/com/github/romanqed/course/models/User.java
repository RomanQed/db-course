package com.github.romanqed.course.models;

import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.postgres.From;
import com.github.romanqed.course.postgres.Getter;
import com.github.romanqed.course.postgres.Setter;
import com.github.romanqed.course.postgres.To;

import java.util.Objects;

@Model("users")
public final class User implements Entity {
    private int id;
    private String login;
    private String password;
    private boolean admin;

    @To
    public static void to(Setter setter, User user) {
        setter.set("login", user.login);
        setter.set("password", user.password);
        setter.set("admin", user.admin);
    }

    @From
    public static void from(Getter getter, User user) {
        user.id = getter.get("id", Integer.class);
        user.login = getter.get("login", String.class);
        user.password = getter.get("password", String.class);
        user.admin = getter.get("admin", Boolean.class);
    }

    public static User of(String login, String password) {
        var ret = new User();
        ret.login = Objects.requireNonNull(login);
        ret.password = Objects.requireNonNull(password);
        return ret;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
