package com.github.romanqed.benchmark;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.io.IOException;

public final class MetricUtil {

    public static Runnable startMetricServer() throws IOException {
        // Start metric server
        var registry = new PrometheusMeterRegistry(
                PrometheusConfig.DEFAULT,
                PrometheusRegistry.defaultRegistry,
                Clock.SYSTEM
        );
        new JvmInfoMetrics().bindTo(registry);
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        var gcm = new JvmGcMetrics();
        gcm.bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new JvmThreadDeadlockMetrics().bindTo(registry);
        var hpm = new JvmHeapPressureMetrics();
        hpm.bindTo(registry);
        var metricPort = Integer.parseInt(System.getenv("METRIC_PORT"));
        System.out.println("METRIC_PORT: " + metricPort);
        var metrics = HTTPServer.builder()
                .port(metricPort)
                .buildAndStart();
        return () -> {
            metrics.close();
            registry.close();
            gcm.close();
            hpm.close();
        };
    }
}
