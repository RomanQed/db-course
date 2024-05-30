package com.github.romanqed.course.models;

import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.postgres.From;
import com.github.romanqed.course.postgres.Getter;
import com.github.romanqed.course.postgres.Setter;
import com.github.romanqed.course.postgres.To;

@Model("exchanges")
public final class Exchange implements Entity {
    private int id;
    private int from;
    private int to;
    private double factor;

    @To
    public static void to(Setter setter, Exchange exchange) {
        setter.set("_from", exchange.from);
        setter.set("_to", exchange.to);
        setter.set("factor", exchange.factor);
    }

    @From
    public static void from(Getter getter, Exchange exchange) {
        exchange.id = getter.get("id", Integer.class);
        exchange.from = getter.get("_from", Integer.class);
        exchange.to = getter.get("_to", Integer.class);
        exchange.factor = getter.get("factor", Double.class);
    }

    public static Exchange of(int from, int to, double factor) {
        var ret = new Exchange();
        ret.from = from;
        ret.to = to;
        ret.factor = factor;
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

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }
}
