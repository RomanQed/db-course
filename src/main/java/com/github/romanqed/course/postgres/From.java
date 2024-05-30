package com.github.romanqed.course.postgres;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Data from bytes
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface From {
}
