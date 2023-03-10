package com.example.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test documentation
 */
@SuppressWarnings("unused") // FIXME does not work, idea does not see this
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestEnvironmentInitializer {
}
