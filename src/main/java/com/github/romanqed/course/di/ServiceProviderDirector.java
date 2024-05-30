package com.github.romanqed.course.di;

import io.github.amayaframework.di.ServiceProvider;
import io.github.amayaframework.di.ServiceProviderBuilder;

public interface ServiceProviderDirector {

    void setBuilder(ServiceProviderBuilder builder);

    ServiceProvider build();
}
