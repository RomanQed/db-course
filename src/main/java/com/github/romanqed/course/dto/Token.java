package com.github.romanqed.course.dto;

public final class Token {
    private String token;
    private Boolean twoFactor;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
        this.token = null;
        this.twoFactor = true;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getTwoFactor() {
        return twoFactor;
    }

    public void setTwoFactor(Boolean twoFactor) {
        this.twoFactor = twoFactor;
    }
}
