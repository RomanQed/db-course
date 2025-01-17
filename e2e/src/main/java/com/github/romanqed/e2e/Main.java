package com.github.romanqed.e2e;

import com.github.romanqed.course.database.Database;
import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.di.ScanProviderDirector;
import com.github.romanqed.course.hash.Encoder;
import com.github.romanqed.course.models.User;
import com.github.romanqed.jfunc.Runnable0;
import com.github.romanqed.jtype.Types;
import io.github.amayaframework.di.*;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final String SYSTEM_ROLE = "_service";

    public static void main(String[] args) throws Throwable {
        var shutdown = createApp(7000);
        shutdown.run();
    }

    private static Runnable0 createApp(int port) {
        // Configure DI
        var repository = new HashRepository();
        var builder = Builders.createChecked();
        builder.setRepository(repository);
        // Stub project logger
        var logger = LoggerFactory.getLogger("main");
        builder.addService(Logger.class, () -> logger);
        // Process all di-dependent actions
        var director = new ScanProviderDirector();
        director.setBuilder(builder);
        var provider = director.build();
        // Get postgres db instance
        var postgres = provider.instantiate(Database.class);
        initAdminUser(provider);
        var javalin = provider.instantiate(Javalin.class);
        javalin.start(port);
        return () -> {
            javalin.stop();
            postgres.close();
        };
    }

    @SuppressWarnings("unchecked")
    private static void initAdminUser(ServiceProvider provider) {
        var encoder = provider.instantiate(Encoder.class);
        var users = (Repository<User>) provider.instantiate(Types.of(Repository.class, User.class));
        if (users.exists(SYSTEM_ROLE, "login", "user")) {
            return;
        }
        var user = User.of("user", encoder.encode("pass"));
        user.setAdmin(true);
        users.put(SYSTEM_ROLE, user);
    }
}
