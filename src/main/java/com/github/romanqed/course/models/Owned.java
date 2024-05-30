package com.github.romanqed.course.models;

public abstract class Owned implements Entity {
    protected int owner;

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }
}
