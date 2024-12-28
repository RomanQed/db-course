package com.github.romanqed.course;

import com.github.romanqed.course.database.Database;
import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.di.ScanProviderDirector;
import com.github.romanqed.course.hash.Encoder;
import com.github.romanqed.course.javalin.ServerConfig;
import com.github.romanqed.course.models.User;
import com.github.romanqed.jtype.Types;
import io.github.amayaframework.di.*;
import io.javalin.Javalin;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public final class Main {
    private static final String SYSTEM_ROLE = "_service";
    private static final String LOGGER_CONFIG = "log4j.properties";

    public static void main(String[] args) throws SQLException {
        // Configure logger
        PropertyConfigurator.configure(LOGGER_CONFIG);
        // Configure DI
        var repository = new HashRepository();
        var builder = Builders.createChecked();
        builder.setRepository(repository);
        // Add project logger
        var logger = LoggerFactory.getLogger("main");
        builder.addService(Logger.class, () -> logger);
        // Process all di-dependent actions
        var director = new ScanProviderDirector();
        director.setBuilder(builder);
        var provider = (ServiceProvider) null;
        // Check DI errors
        try {
            provider = director.build();
        } catch (TypeNotFoundException e) {
            logger.error("DI cannot find type {}", e.getType());
            throw e;
        } catch (CycleFoundException e) {
            logger.error("DI found cycle: {}", e.getCycle());
            throw e;
        }
        // Get postgres db instance
        var postgres = provider.instantiate(Database.class);
        // Init admin user
        initAdminUser(provider, logger);
        // Start Javalin instance
        var javalin = startJavalin(provider, logger);
        // Bind stop actions by exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            javalin.stop();
            postgres.close();
        }));
    }

    private static Javalin startJavalin(ServiceProvider provider, Logger logger) {
        var javalin = provider.instantiate(Javalin.class);
        var config = provider.instantiate(ServerConfig.class);
        var host = config.getHost();
        var port = config.getPort();
        if (host == null || host.isEmpty()) {
            javalin.start(port);
            logger.info("Javalin instance successfully started on localhost:{}", port);
        } else {
            javalin.start(host, port);
            logger.info("Javalin instance successfully started on {}:{}", host, port);
        }
        return javalin;
    }

    @SuppressWarnings("unchecked")
    private static void initAdminUser(ServiceProvider provider, Logger logger) {
        var config = provider.instantiate(ServerConfig.class);
        var encoder = provider.instantiate(Encoder.class);
        var users = (Repository<User>) provider.instantiate(Types.of(Repository.class, User.class));
        if (users.exists(SYSTEM_ROLE, "login", config.getLogin())) {
            logger.info("Admin user exists");
            return;
        }
        var login = config.getLogin();
        var user = User.of(login, encoder.encode(config.getPassword()));
        user.setAdmin(true);
        users.put(SYSTEM_ROLE, user);
        logger.info("Admin user successfully created with id {}", user.getId());
    }
}
