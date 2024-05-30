package com.github.romanqed.course.dto;

public class ValidateException extends IllegalArgumentException {

    public ValidateException(String message) {
        super(message);
    }
}
