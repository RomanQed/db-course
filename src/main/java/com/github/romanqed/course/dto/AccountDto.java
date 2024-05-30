package com.github.romanqed.course.dto;

public final class AccountDto implements Validated {
    private Integer currency;
    private String description;
    private Double value;

    public Integer getCurrency() {
        return currency;
    }

    public void setCurrency(Integer currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public void validate() throws ValidateException {
        if (currency == null || currency < 1) {
            throw new ValidateException("Invalid currency id");
        }
        if (value == null) {
            throw new ValidateException("Invalid value");
        }
    }
}
