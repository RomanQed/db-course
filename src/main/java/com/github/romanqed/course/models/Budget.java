package com.github.romanqed.course.models;

import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.postgres.From;
import com.github.romanqed.course.postgres.Getter;
import com.github.romanqed.course.postgres.Setter;
import com.github.romanqed.course.postgres.To;

import java.util.Date;

@Model("budgets")
public final class Budget extends Owned implements Entity {
    private int id;
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

    public static Budget of(int owner, int currency, double value) {
        var ret = new Budget();
        ret.owner = owner;
        ret.currency = currency;
        ret.value = value;
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
