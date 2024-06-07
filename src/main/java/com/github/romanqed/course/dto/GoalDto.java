package com.github.romanqed.course.dto;

public final class GoalDto implements Validated {
    private Integer account;
    private String description;
    private Double target;

    public Integer getAccount() {
        return account;
    }

    public void setAccount(Integer account) {
        this.account = account;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getTarget() {
        return target;
    }

    public void setTarget(Double target) {
        this.target = target;
    }

    @Override
    public void validate() throws ValidateException {
        if (account == null || account < 1) {
            throw new ValidateException("Invalid account id");
        }
        if (target == null || target <= 0) {
            throw new ValidateException("Invalid target value");
        }
    }
}
