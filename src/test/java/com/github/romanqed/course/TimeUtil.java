package com.github.romanqed.course;

import java.util.Calendar;
import java.util.Date;

public final class TimeUtil {
    private TimeUtil() {
    }

    public static Date nullifyTime(Date date) {
        var raw = Calendar.getInstance();
        raw.setTime(date);
        var ret = Calendar.getInstance();
        ret.setTimeInMillis(0);
        ret.set(Calendar.HOUR_OF_DAY, 0);
        ret.set(Calendar.YEAR, raw.get(Calendar.YEAR));
        ret.set(Calendar.MONTH, raw.get(Calendar.MONTH));
        ret.set(Calendar.DAY_OF_MONTH, raw.get(Calendar.DAY_OF_MONTH));
        return ret.getTime();
    }

    public static Date ofYear(int year) {
        var calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return nullifyTime(calendar.getTime());
    }
}
