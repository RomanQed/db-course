package com.github.romanqed.course.dto;

public final class ExchangeDto implements Validated {
    private Integer from;
    private Integer to;
    private Double factor;

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

    public Double getFactor() {
        return factor;
    }

    public void setFactor(Double factor) {
        this.factor = factor;
    }

    @Override
    public void validate() throws ValidateException {
        if (from == null && to == null) {
            throw new ValidateException("Invalid exchange accounts");
        }
        if (factor == null) {
            throw new ValidateException("Factor must be non-null");
        }
    }
}
