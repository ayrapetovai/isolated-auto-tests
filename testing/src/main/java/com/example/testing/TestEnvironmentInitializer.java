package com.example.testing;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Test documentation
 */
@SuppressWarnings("unused") // FIXME does not work, idea does not see this
@Retention(RetentionPolicy.RUNTIME)
public @interface TestEnvironmentInitializer {
}
