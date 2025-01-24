package com.github.romanqed.course.javalin;

import com.github.romanqed.course.di.ProviderConsumer;
import com.github.romanqed.course.di.ServiceProviderConsumer;
import com.github.romanqed.course.util.Util;
import io.github.amayaframework.di.ServiceProvider;
import io.github.amayaframework.di.ServiceProviderBuilder;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.atteo.classindex.ClassIndex;

import java.io.File;
import java.util.Objects;

@ProviderConsumer
public final class JavalinServiceConsumer implements ServiceProviderConsumer {
    private static final File JAVALIN_CONFIG = new File("server.json");
    private static final HandlerFactory HANDLER_FACTORY = new JeflectHandlerFactory();
    private Iterable<Class<?>> classes; // Found controller classes

    private static void processControllerClass(ServiceProvider provider, Javalin javalin, Class<?> clazz) {
        var object = provider.get(clazz);
        var processed = HANDLER_FACTORY.create(object);
        processed.forEach((data, handler) -> javalin.addHttpHandler(data.getType(), data.getRoute(), handler));
    }

    @Override
    public void pre(ServiceProviderBuilder builder) {
        var config = Util.read(JAVALIN_CONFIG, ServerConfig.class);
        Objects.requireNonNull(config.getLogin());
        Objects.requireNonNull(config.getPassword());
        builder.addInstance(ServerConfig.class, config);
        var javalin = Javalin.create(e ->
                e.bundledPlugins.enableCors(cors ->
                        cors.addRule(CorsPluginConfig.CorsRule::anyHost)
                )
        );
        javalin.after(new CorsHandler());
        builder.addInstance(Javalin.class, javalin);
        classes = ClassIndex.getAnnotated(JavalinController.class);
        for (var clazz : classes) {
            builder.addSingleton(clazz);
        }
    }

    @Override
    public void post(ServiceProvider provider) {
        var javalin = provider.get(Javalin.class);
        var config = javalin.unsafeConfig();
        config.jsonMapper(provider.get(JsonMapper.class));
        for (var clazz : classes) {
            processControllerClass(provider, javalin, clazz);
        }
    }
}
