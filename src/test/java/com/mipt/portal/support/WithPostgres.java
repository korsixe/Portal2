package com.mipt.portal.support;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that wires {@link PostgresExtension} into a JUnit 5 test class.
 * Patterned after the example in lecture #11.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(PostgresExtension.class)
public @interface WithPostgres {
}
