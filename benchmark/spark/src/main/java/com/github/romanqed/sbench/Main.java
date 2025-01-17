package com.github.romanqed.sbench;

import com.github.romanqed.benchmark.Data;
import com.github.romanqed.benchmark.MetricUtil;
import com.google.gson.Gson;
import spark.Spark;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            System.exit(1);
        });
        // Start metrics
        var metrics = MetricUtil.startMetricServer();
        // Start spark
        var port = Integer.parseInt(System.getenv("PORT"));
        Spark.port(port);
        var gson = new Gson();
        Spark.get("/get", (req, res) -> {
            var rsp = new ArrayList<Data>(50);
            for (var i = 0; i < 50; ++i) {
                rsp.add(new Data(i, "Item #" + i));
            }
            res.header("Content-Type", "application/json");
            return gson.toJson(rsp);
        });
        int[] hole = new int[1];
        Spark.post("/post", (req, res) -> {
            var body = req.bodyAsBytes();
            hole[0] += body.length;
            res.status(200);
            return "Success";
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Spark.stop();
            metrics.run();
            System.out.println("Black-hole value: " + hole[0]);
        }));
    }
}
