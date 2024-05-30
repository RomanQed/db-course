package com.github.romanqed.course.models;

import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.postgres.From;
import com.github.romanqed.course.postgres.Getter;
import com.github.romanqed.course.postgres.Setter;
import com.github.romanqed.course.postgres.To;

@Model("categories")
public final class Category implements Entity {
    private int id;
    private String name;

    @To
    public static void to(Setter setter, Category category) {
        setter.set("name", category.name);
    }

    @From
    public static void from(Getter getter, Category category) {
        category.id = getter.get("id", Integer.class);
        category.name = getter.get("name", String.class);
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
