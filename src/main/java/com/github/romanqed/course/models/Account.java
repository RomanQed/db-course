package com.github.romanqed.course.models;

import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.postgres.From;
import com.github.romanqed.course.postgres.Getter;
import com.github.romanqed.course.postgres.Setter;
import com.github.romanqed.course.postgres.To;

@Model("accounts")
public final class Account implements Entity {
    private int id;
    private int owner;
    private int currency;
    private String description;
    private double value;

    @To
    public static void to(Setter setter, Account account) {
        setter.set("owner", account.owner);
        setter.set("currency", account.currency);
        setter.set("description", account.description);
        setter.set("value", account.value);
    }

    @From
    public static void from(Getter getter, Account account) {
        account.id = getter.get("id", Integer.class);
        account.owner = getter.get("owner", Integer.class);
        account.currency = getter.get("currency", Integer.class);
        account.description = getter.get("description", String.class);
        account.value = getter.get("value", Double.class);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
