package com.github.romanqed.jbench;

import com.github.romanqed.benchmark.Data;
import com.github.romanqed.benchmark.MetricUtil;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;

public final class Main {

    public static void main(String[] args) throws IOException {
        // Start metric server
        var metrics = MetricUtil.startMetricServer();
        // Start javalin
        var port = Integer.parseInt(System.getenv("PORT"));
        var javalin = Javalin.create();
        javalin.get("/get", ctx -> {
            var rsp = new ArrayList<Data>(50);
            for (var i = 0; i < 50; ++i) {
                rsp.add(new Data(i, "Item #" + i));
            }
            ctx.json(rsp);
        });
        int[] hole = new int[1];
        javalin.post("/post", ctx -> {
            var body = ctx.bodyAsBytes();
            hole[0] += body.length;
            ctx.status(HttpStatus.OK);
            ctx.result("Success");
        });
        javalin.start(port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            javalin.stop();
            metrics.run();
            System.out.println("Black-hole value: " + hole[0]);
        }));
    }
}
