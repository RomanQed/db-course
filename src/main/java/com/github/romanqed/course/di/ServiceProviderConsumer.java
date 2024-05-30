package com.github.romanqed.course.di;

import io.github.amayaframework.di.ServiceProvider;
import io.github.amayaframework.di.ServiceProviderBuilder;

public interface ServiceProviderConsumer {

    void pre(ServiceProviderBuilder builder) throws Throwable;

    void post(ServiceProvider provider) throws Throwable;
}
