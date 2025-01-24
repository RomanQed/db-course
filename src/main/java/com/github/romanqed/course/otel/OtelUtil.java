package com.github.romanqed.course.otel;

import com.github.romanqed.jfunc.Exceptions;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public final class OtelUtil {
    private static final String JAEGER_ENDPOINT = "JAEGER_ENDPOINT";
    private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
    private static final AttributeKey<String> HOST_NAME = AttributeKey.stringKey("host.name");
    private static final AttributeKey<String> HOST_TYPE = AttributeKey.stringKey("host.type");
    private static final AttributeKey<String> OS_NAME = AttributeKey.stringKey("os.name");
    private static final AttributeKey<String> OS_VERSION = AttributeKey.stringKey("os.version");

    private OtelUtil() {
    }

    private static Attributes getServiceAttributes() {
        var hostName = Exceptions.suppress(() -> InetAddress.getLocalHost().getHostName(), t -> "unknown");
        var hostType = System.getProperty("os.arch");
        var osName = System.getProperty("os.name");
        var osVersion = System.getProperty("os.version");
        return Attributes.of(
                SERVICE_NAME, "DbCourse",
                HOST_NAME, hostName,
                HOST_TYPE, hostType,
                OS_NAME, osName,
                OS_VERSION, osVersion
        );
    }

    public static OpenTelemetry createOtel(String endpoint) {
        // Create exporter
        var exporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(endpoint)
                .setTimeout(30, TimeUnit.SECONDS)
                .build();
        // Get service attributes
        var attributes = getServiceAttributes();
        var resource = Resource.create(attributes);
        // Create provider
        var provider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
                .setResource(Resource.getDefault().merge(resource))
                .build();
        // Create otel
        var ret = OpenTelemetrySdk.builder()
                .setTracerProvider(provider)
                .build();
        // Register hook
        Runtime.getRuntime().addShutdownHook(new Thread(ret::close));
        return ret;
    }

    public static OpenTelemetry createOtel() {
        var endpoint = System.getenv(JAEGER_ENDPOINT);
        if (endpoint == null) {
            return OpenTelemetry.noop();
        }
        return createOtel(endpoint);
    }
}
