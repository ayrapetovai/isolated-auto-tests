package com.example.testing;

import com.example.testing.template.TestEnvironmentLifeCycleController;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runners.Parameterized;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A test class that have @IsolatedAutoTest on it,
 * also must have a method with @TestEnvironmentInitializer.
 */
@Parameterized.UseParametersRunnerFactory
@ExtendWith({TestEnvironmentLifeCycleController.class})
// DEFINED_PORT makes MVC run without mocks
@SpringBootTest(classes = {TestingApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
// Exclude JDBC auto configurations, because we set properties in "runtime"
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IsolatedAutoTest {
}
