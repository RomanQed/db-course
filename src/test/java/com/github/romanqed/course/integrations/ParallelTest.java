package com.github.romanqed.course.integrations;

import org.junit.jupiter.api.Test;

public final class ParallelTest {

    @Test
    public void runAllTests() throws InterruptedException {
        var ait = new AuthorizationITCase();
        var bit = new BudgetITCase();
        var eit = new ExchangeITCase();
        var t1 = new Thread(() -> {
            try {
                AuthorizationITCase.init();
                ait.test();
                AuthorizationITCase.destroy();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        var t2 = new Thread(() -> {
            try {
                BudgetITCase.init();
                bit.test();
                BudgetITCase.destroy();
            } catch (Throwable e) {
                throw new RuntimeException();
            }
        });
        var t3 = new Thread(() -> {
            try {
                ExchangeITCase.init();
                eit.test();
                ExchangeITCase.destroy();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }
}
