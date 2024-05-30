package com.github.romanqed.course.dto;

public final class Credentials implements Validated {
    private String login;
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void validate() throws ValidateException {
        if (login == null) {
            throw new ValidateException("Missing login");
        }
        if (password == null) {
            throw new ValidateException("Missing password");
        }
    }
}
