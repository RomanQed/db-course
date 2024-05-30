package com.github.romanqed.course.dto;

public final class GoalStatus {
    private double percents;
    private double reached;
    private double remained;

    public double getPercents() {
        return percents;
    }

    public void setPercents(double percents) {
        this.percents = percents;
    }

    public double getReached() {
        return reached;
    }

    public void setReached(double reached) {
        this.reached = reached;
    }

    public double getRemained() {
        return remained;
    }

    public void setRemained(double remained) {
        this.remained = remained;
    }
}
