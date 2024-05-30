package com.github.romanqed.course.dto;

public final class BudgetStatus {
    private double spent;
    private double got;
    private double total;

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public double getGot() {
        return got;
    }

    public void setGot(double got) {
        this.got = got;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
