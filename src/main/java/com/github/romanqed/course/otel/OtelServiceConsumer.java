package com.github.romanqed.course.otel;

import com.github.romanqed.course.di.ProviderConsumer;
import com.github.romanqed.course.di.ServiceProviderConsumer;
import io.github.amayaframework.di.ServiceProvider;
import io.github.amayaframework.di.ServiceProviderBuilder;
import io.opentelemetry.api.OpenTelemetry;

@ProviderConsumer
public final class OtelServiceConsumer implements ServiceProviderConsumer {

    @Override
    public void pre(ServiceProviderBuilder builder) throws Throwable {
        var endpoint = System.getenv("JAEGER_ENDPOINT");
        if (endpoint == null) {
            builder.addInstance(OpenTelemetry.class, OpenTelemetry.noop());
            return;
        }

    }

    @Override
    public void post(ServiceProvider provider) throws Throwable {

    }
}
