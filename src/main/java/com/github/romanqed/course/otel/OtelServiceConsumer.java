package com.github.romanqed.course.otel;

import com.github.romanqed.course.di.ProviderConsumer;
import com.github.romanqed.course.di.ServiceProviderConsumer;
import io.github.amayaframework.di.ServiceProvider;
import io.github.amayaframework.di.ServiceProviderBuilder;
import io.opentelemetry.api.OpenTelemetry;

import java.util.Optional;

@ProviderConsumer
public final class OtelServiceConsumer implements ServiceProviderConsumer {
    private static final String SERVICE_NAME = "OTEL_SERVICE_NAME";
    private static final String DEFAULT_SERVICE_NAME = "DbCourse";

    @Override
    public void pre(ServiceProviderBuilder builder) {
        var name = Optional.ofNullable(System.getenv(SERVICE_NAME));
        var telemetry = OtelUtil.createOtel(name.orElse(DEFAULT_SERVICE_NAME));
        builder.addInstance(OpenTelemetry.class, telemetry);
    }

    @Override
    public void post(ServiceProvider provider) throws Throwable {
    }
}
