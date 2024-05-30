package com.github.romanqed.course.models;

import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.postgres.From;
import com.github.romanqed.course.postgres.Getter;
import com.github.romanqed.course.postgres.Setter;
import com.github.romanqed.course.postgres.To;

import java.util.Date;

@Model("transactions")
public final class Transaction implements Entity {
    private int id;
    private int owner;
    private int category;
    private Integer from; // account
    private Integer to; // account
    private double value;
    private String description;
    private Date timestamp;

    @To
    public static void to(Setter setter, Transaction transaction) {
        setter.set("owner", transaction.owner);
        setter.set("category", transaction.category);
        setter.set("_from", transaction.from);
        setter.set("_to", transaction.to);
        setter.set("value", transaction.value);
        setter.set("description", transaction.description);
        setter.set("_timestamp", transaction.timestamp);
    }

    @From
    public static void from(Getter getter, Transaction transaction) {
        transaction.id = getter.get("id", Integer.class);
        transaction.owner = getter.get("owner", Integer.class);
        transaction.category = getter.get("category", Integer.class);
        transaction.from = getter.get("_from", Integer.class);
        transaction.to = getter.get("_to", Integer.class);
        transaction.value = getter.get("value", Double.class);
        transaction.description = getter.get("description", String.class);
        transaction.timestamp = getter.get("_timestamp", Date.class);
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

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
