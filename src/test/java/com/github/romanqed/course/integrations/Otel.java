package com.github.romanqed.course.integrations;

import com.github.romanqed.course.otel.OtelUtil;
import io.opentelemetry.api.OpenTelemetry;

final class Otel {
    static final OpenTelemetry TELEMETRY = OtelUtil.createOtel("ITCases");

    private Otel() {
    }
}
