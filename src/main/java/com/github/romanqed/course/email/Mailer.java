package com.github.romanqed.course.email;

public interface Mailer {

    void send(String address, String message);
}
