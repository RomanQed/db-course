package com.github.romanqed.benchmark;

import com.github.romanqed.jfunc.Exceptions;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

final class Util {
    private static final ObjectName OS_NAME = Exceptions.suppress(
            () -> ObjectName.getInstance("java.lang:type=OperatingSystem")
    );

    private Util() {
    }

    static double getProcessCpuLoad(MBeanServer mbs) throws Exception {
        var list = mbs.getAttributes(OS_NAME, new String[]{"ProcessCpuLoad"});

        if (list.isEmpty()) {
            return 0.0;
        }
        var att = (Attribute) list.get(0);
        var value = (Double) att.getValue();

        if (value < 0.0) {
            return 0.0;  // usually takes a couple of seconds before we get real values
        }
        return ((int) (value * 1000) / 10.0); // returns a percentage value with 1 decimal point precision
    }
}
