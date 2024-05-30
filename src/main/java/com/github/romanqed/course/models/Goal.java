package com.github.romanqed.course.models;

import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.postgres.From;
import com.github.romanqed.course.postgres.Getter;
import com.github.romanqed.course.postgres.Setter;
import com.github.romanqed.course.postgres.To;

@Model("goals")
public final class Goal extends Owned implements Entity {
    private int id;
    private int account;
    private String description;
    private double target;

    @To
    public static void to(Setter setter, Goal goal) {
        setter.set("owner", goal.owner);
        setter.set("account", goal.account);
        setter.set("description", goal.description);
        setter.set("target", goal.target);
    }

    @From
    public static void from(Getter getter, Goal goal) {
        goal.id = getter.get("id", Integer.class);
        goal.owner = getter.get("owner", Integer.class);
        goal.account = getter.get("account", Integer.class);
        goal.description = getter.get("description", String.class);
        goal.target = getter.get("target", Double.class);
    }

    public static Goal of(int owner, int account, double target) {
        var ret = new Goal();
        ret.owner = owner;
        ret.account = account;
        ret.target = target;
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

    public int getAccount() {
        return account;
    }

    public void setAccount(int account) {
        this.account = account;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }
}
