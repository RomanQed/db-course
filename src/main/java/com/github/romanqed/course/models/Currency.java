package com.github.romanqed.course.models;

import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.postgres.From;
import com.github.romanqed.course.postgres.Getter;
import com.github.romanqed.course.postgres.Setter;
import com.github.romanqed.course.postgres.To;

import java.util.Objects;

@Model("currencies")
public final class Currency implements Entity {
    private int id;
    private String name;

    @To
    public static void to(Setter setter, Currency currency) {
        setter.set("name", currency.name);
    }

    @From
    public static void from(Getter getter, Currency currency) {
        currency.id = getter.get("id", Integer.class);
        currency.name = getter.get("name", String.class);
    }

    public static Currency of(String name) {
        var ret = new Currency();
        ret.name = Objects.requireNonNull(name);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
