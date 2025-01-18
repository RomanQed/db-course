package com.github.romanqed.course.dto;

public final class TwoFactorDto implements Validated {
    private String login;
    private String code;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public void validate() throws ValidateException {
        if (login == null) {
            throw new ValidateException("Missing login");
        }
        if (code == null) {
            throw new ValidateException("Missing code");
        }
    }
}
