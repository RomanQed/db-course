package com.github.romanqed.course.otel;

import com.github.romanqed.jfunc.Exceptions;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public final class OtelUtil {
    private static final String JAEGER_ENDPOINT = "JAEGER_ENDPOINT";
    private static final String METRICS_PORT = "METRICS_PORT";
    private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
    private static final AttributeKey<String> HOST_NAME = AttributeKey.stringKey("host.name");
    private static final AttributeKey<String> HOST_TYPE = AttributeKey.stringKey("host.type");
    private static final AttributeKey<String> OS_NAME = AttributeKey.stringKey("os.name");
    private static final AttributeKey<String> OS_VERSION = AttributeKey.stringKey("os.version");

    private OtelUtil() {
    }

    private static Runnable addResourceMetrics(MeterRegistry registry) {
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
        return () -> {
            gcm.close();
            hpm.close();
        };
    }

    private static Attributes getServiceAttributes(String app) {
        var hostName = Exceptions.suppress(() -> InetAddress.getLocalHost().getHostName(), t -> "unknown");
        var hostType = System.getProperty("os.arch");
        var osName = System.getProperty("os.name");
        var osVersion = System.getProperty("os.version");
        return Attributes.of(
                SERVICE_NAME, app,
                HOST_NAME, hostName,
                HOST_TYPE, hostType,
                OS_NAME, osName,
                OS_VERSION, osVersion
        );
    }

    private static SdkMeterProvider createMeterProvider() {
        var raw = System.getenv(METRICS_PORT);
        if (raw == null) {
            return null;
        }
        var port = Exceptions.suppress(() -> Integer.parseInt(raw), t -> null);
        if (port == null) {
            return null;
        }
        var server = PrometheusHttpServer.builder()
                .setPort(port)
                .build();
        return SdkMeterProvider.builder()
                .registerMetricReader(server)
                .build();
    }

    public static OpenTelemetry createOtel(String endpoint, String app) {
        // Create exporter
        var exporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(endpoint)
                .setTimeout(30, TimeUnit.SECONDS)
                .build();
        // Get service attributes
        var attributes = getServiceAttributes(app);
        var resource = Resource.create(attributes);
        // Create provider
        var tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
                .setResource(Resource.getDefault().merge(resource))
                .build();
        var ret = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(ret::close));
        return ret;
//        var meterProvider = createMeterProvider();
//        if (meterProvider == null) {
//            var ret = OpenTelemetrySdk.builder()
//                    .setTracerProvider(tracerProvider)
//                    .build();
//            Runtime.getRuntime().addShutdownHook(new Thread(ret::close));
//            return ret;
//        }
//        var ret = OpenTelemetrySdk.builder()
//                .setTracerProvider(tracerProvider)
//                .setMeterProvider(meterProvider)
//                .build();
//        var registry = OpenTelemetryMeterRegistry.builder(ret)
//                .setPrometheusMode(true)
//                .build();
//        var closer = addResourceMetrics(registry);
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            ret.close();
//            registry.close();
//            closer.run();
//        }));
//        return ret;
    }

    public static OpenTelemetry createOtel(String app) {
        var endpoint = System.getenv(JAEGER_ENDPOINT);
        if (endpoint == null) {
            return OpenTelemetry.noop();
        }
        return createOtel(endpoint, app);
    }
}
