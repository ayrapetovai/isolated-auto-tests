package com.example.testing.template;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runners.Parameterized;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@Parameterized.UseParametersRunnerFactory
@ExtendWith({TestConfigParameterResolver.class})
// DEFINED_PORT makes MVC run without mocks
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
// Exclude JDBC auto configurations, because we set properties in "runtime"
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public abstract class TestTemplate {

	@BeforeAll
	public static void beforeAll(TestEnvironment testEnvironment) {

	}

	@AfterAll
	public static void afterAll(TestEnvironment testEnvironment) {

	}

	@BeforeEach
	public final void beforeEach(TestEnvironment testEnvironment) {
		setUpTestEnvironment(testEnvironment);
		testEnvironment.createAndAwait();
	}

	@AfterEach
	public final void afterEach(TestEnvironment testEnvironment) {
		testEnvironment.close();
	}

	public abstract void setUpTestEnvironment(TestEnvironment testEnvironment);
}
