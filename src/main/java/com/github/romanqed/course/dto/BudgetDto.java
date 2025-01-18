package com.github.romanqed.course.dto;

import java.util.Date;

public final class BudgetDto implements Validated {
    private Integer currency;
    private Date start;
    private Date end;
    private String description;
    private Double value;

    public Integer getCurrency() {
        return currency;
    }

    public void setCurrency(Integer currency) {
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    private void checkTimeRange() {
        if (start == null || start.equals(end) || end == null || end.before(start)) {
            throw new ValidateException("Invalid time ranges");
        }
    }

    @Override
    public void validate() throws ValidateException {
        if (currency == null || currency < 1) {
            throw new ValidateException("Invalid currency id");
        }
        checkTimeRange();
        if (value == null || value < 0) {
            throw new ValidateException("Invalid value");
        }
    }
}
