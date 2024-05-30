package com.github.romanqed.course.models;

import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.postgres.From;
import com.github.romanqed.course.postgres.Getter;
import com.github.romanqed.course.postgres.Setter;
import com.github.romanqed.course.postgres.To;

import java.util.Date;

@Model("budgets")
public final class Budget implements Entity {
    private int id;
    private int owner;
    private int currency;
    private Date start;
    private Date end;
    private String description;
    private double value;

    @To
    public static void to(Setter setter, Budget budget) {
        setter.set("owner", budget.owner);
        setter.set("currency", budget.currency);
        setter.set("_start", budget.start);
        setter.set("_end", budget.end);
        setter.set("description", budget.description);
        setter.set("value", budget.value);
    }

    @From
    public static void from(Getter getter, Budget budget) {
        budget.id = getter.get("id", Integer.class);
        budget.owner = getter.get("owner", Integer.class);
        budget.currency = getter.get("currency", Integer.class);
        budget.start = getter.get("_start", Date.class);
        budget.end = getter.get("_end", Date.class);
        budget.description = getter.get("description", String.class);
        budget.value = getter.get("value", Double.class);
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

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
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
