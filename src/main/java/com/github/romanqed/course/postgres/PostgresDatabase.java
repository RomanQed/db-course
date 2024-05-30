package com.github.romanqed.course.postgres;

import com.github.romanqed.course.database.Database;
import com.github.romanqed.course.database.Model;
import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.models.Entity;
import com.github.romanqed.jeflect.meta.LambdaType;
import com.github.romanqed.jeflect.meta.MetaFactory;
import com.github.romanqed.jfunc.Exceptions;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.Arrays;
import java.util.function.Supplier;

final class PostgresDatabase implements Database {
    private static final MetaFactory FACTORY = new MetaFactory();
    @SuppressWarnings("rawtypes")
    private static final LambdaType<Supplier> SUPPLIER = LambdaType.fromClass(Supplier.class);
    private static final LambdaType<Serializer> SERIALIZER = LambdaType.fromClass(Serializer.class);
    private static final LambdaType<Deserializer> DESERIALIZER = LambdaType.fromClass(Deserializer.class);

    private final Connection connection;

    PostgresDatabase(Connection connection) {
        this.connection = connection;
    }

    @SuppressWarnings("unchecked")
    private <V extends Entity> Repository<V> innerCreate(Class<V> type) throws Throwable {
        var model = type.getAnnotation(Model.class);
        var ctor = type.getConstructor();
        var supplier = (Supplier<V>) FACTORY.packLambdaConstructor(SUPPLIER, ctor);
        var methods = type.getMethods();
        var to = Arrays
                .stream(methods)
                .filter(m -> Modifier.isStatic(m.getModifiers()) && m.isAnnotationPresent(To.class))
                .findFirst()
                .orElseThrow();
        var from = Arrays
                .stream(methods)
                .filter(m -> Modifier.isStatic(m.getModifiers()) && m.isAnnotationPresent(From.class))
                .findFirst()
                .orElseThrow();
        var serializer = FACTORY.packLambdaMethod(SERIALIZER, to);
        var deserializer = FACTORY.packLambdaMethod(DESERIALIZER, from);
        return new PostgresRepository<>(connection, model.value(), supplier, serializer, deserializer);
    }

    @Override
    public <V extends Entity> Repository<V> create(Class<V> type) {
        return Exceptions.suppress(() -> innerCreate(type));
    }

    @Override
    public void close() {
        Exceptions.suppress(connection::close);
    }
}
