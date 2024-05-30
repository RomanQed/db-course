package com.github.romanqed.course.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;

public final class JwtUser {
    private int id;
    private String login;
    private boolean admin;

    public JwtUser(int id, String login, boolean admin) {
        this.id = id;
        this.login = login;
        this.admin = admin;
    }

    public static JwtUser fromJWT(DecodedJWT jwt) {
        var id = jwt.getClaim("id").asInt();
        var login = jwt.getClaim("login").asString();
        var admin = jwt.getClaim("admin").asBoolean();
        return new JwtUser(id, login, admin);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        return "JwtUser{" +
                "id='" + id + '\'' +
                ", email='" + login + '\'' +
                ", admin=" + admin +
                '}';
    }
}
