package com.github.romanqed.course.postgres;

@FunctionalInterface
public interface Getter {
    Object rawGet(String name, Class<?> type);

    @SuppressWarnings("unchecked")
    default <T> T get(String name, Class<T> type) {
        return (T) rawGet(name, type);
    }
}
