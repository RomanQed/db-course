package com.github.romanqed.course.email;

import com.github.romanqed.course.di.ProviderConsumer;
import com.github.romanqed.course.di.ServiceProviderConsumer;
import io.github.amayaframework.di.ServiceProvider;
import io.github.amayaframework.di.ServiceProviderBuilder;

@ProviderConsumer
public class MailerServiceConsumer implements ServiceProviderConsumer {

    @Override
    public void pre(ServiceProviderBuilder builder) {
        // TODO Replace stub somehow
        builder.addSingleton(Mailer.class, LocalMailerStub.class);
    }

    @Override
    public void post(ServiceProvider provider) throws Throwable {
    }
}
