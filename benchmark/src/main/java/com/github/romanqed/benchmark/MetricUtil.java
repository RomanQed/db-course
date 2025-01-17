package com.github.romanqed.benchmark;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;

public final class MetricUtil {
    private static final Gauge CPU_USAGE = Gauge.builder()
            .name("cpu_usage_percentage")
            .help("Current CPU usage percentage")
            .register();

    private static final Gauge MEM_DELTA = Gauge.builder()
            .name("mem_delta")
            .help("Current memory usage value calculated as Runtime.totalMemory - Runtime.freeMemory")
            .register();

    public static Runnable startMetricServer() throws IOException {
        // Start metric server
        var mbs = ManagementFactory.getPlatformMBeanServer();
        var timer = new Timer();
        var runtime = Runtime.getRuntime();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    var load = Util.getProcessCpuLoad(mbs);
                    CPU_USAGE.set(load);
                    MEM_DELTA.set(runtime.totalMemory() - runtime.freeMemory());
                } catch (Error | RuntimeException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 1000);
        JvmMetrics.builder().register();
        var metricPort = Integer.parseInt(System.getenv("METRIC_PORT"));
        var metrics = HTTPServer.builder()
                .port(metricPort)
                .buildAndStart();
        return metrics::close;
    }
}
