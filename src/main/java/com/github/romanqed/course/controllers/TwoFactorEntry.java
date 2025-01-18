package com.github.romanqed.course.controllers;

import java.util.Calendar;
import java.util.Date;

final class TwoFactorEntry {
    private final String code;
    private final Date expire;

    public TwoFactorEntry(String code, int lifetime) {
        this.code = code;
        this.expire = addMinutes(new Date(), lifetime);
    }

    private static Date addMinutes(Date date, int value) {
        var calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, value);
        return calendar.getTime();
    }

    public String getCode() {
        return code;
    }

    public boolean isExpired() {
        return expire.before(new Date());
    }
}
