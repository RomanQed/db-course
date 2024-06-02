package com.github.romanqed.course.controllers;

import java.util.Date;

final class Range {
    private final Date from;
    private final Date to;

    Range(Date from, Date to) {
        this.from = from;
        this.to = to;
    }

    Date getFrom() {
        return from;
    }

    Date getTo() {
        return to;
    }
}
