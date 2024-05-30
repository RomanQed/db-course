package com.github.romanqed.course.dto;

public final class NameDto implements Validated {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void validate() throws ValidateException {
        if (name == null) {
            throw new ValidateException("Name is null");
        }
    }
}
