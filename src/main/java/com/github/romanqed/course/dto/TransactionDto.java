package com.github.romanqed.course.dto;

import java.util.Objects;

public final class TransactionDto implements Validated {
    private Integer category;
    private Integer from; // account
    private Integer to; // account
    private Double value;
    private String description;

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void validate() throws ValidateException {
        if (category == null) {
            throw new ValidateException("Invalid category");
        }
        if ((from == null && to == null) || Objects.equals(from, to)) {
            throw new ValidateException("Invalid source and target");
        }
        if (value == null || value == 0) {
            throw new ValidateException("Zero transaction");
        }
    }
}
