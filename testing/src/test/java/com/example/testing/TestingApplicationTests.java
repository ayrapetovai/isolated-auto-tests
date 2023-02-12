package com.example.testing;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = {TestingApplicationTests.PropertiesInitializer.class})
class TestingApplicationTests {

	public static PostgreSQLContainer<?> postgreSQLContainer;
	public static GenericContainer<?> serviceContainer;

	@BeforeAll
	public static void beforeAll() {
		var postgresPort = 5432;
		postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
				.withDatabaseName("postgres")
				.withUsername("my_db_user")
				.withPassword("my_db_user_password")
				.withEnv("PGPORT", String.valueOf(postgresPort))
				.withExposedPorts(postgresPort);
		postgreSQLContainer.start();
		postgreSQLContainer.waitingFor(Wait.forListeningPort());

		var serviceContainerPort = 8080;
		serviceContainer = new GenericContainer("com.example/demo:latest")
				.withExposedPorts(serviceContainerPort)
				.withEnv("PORT", String.valueOf(serviceContainerPort))
				.withEnv("GREETINGS_URL", "http://host.docker.internal:8090/greetings/")
				.withEnv("JDBC_URL", "jdbc:postgresql://host.docker.internal:" + postgreSQLContainer.getMappedPort(postgresPort) + "/postgres")
				.withEnv("DB_USER", "my_db_user")
				.withEnv("DB_PASS", "my_db_user_password");
		serviceContainer.start();
		serviceContainer.waitingFor(Wait.forListeningPort());
	}

	static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
					"spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
					"spring.datasource.username=" + postgreSQLContainer.getUsername(),
					"spring.datasource.password=" + postgreSQLContainer.getPassword(),
					"spring.datasource.driver=" + postgreSQLContainer.getDriverClassName()
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@AfterAll
	public static void afterAll() {
		serviceContainer.stop();
		postgreSQLContainer.stop();
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private RestMock restMock;

	@AfterEach
	public final void afterEach() {
		restMock.close();
	}

	public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		log.info("query: {}", sql);
		return jdbcTemplate.query(sql, rowMapper);
	}

	public void mockRest(String uriRegexp, Function<RequestData, Object> action) {
		log.info("mock rest endpoint `{}`", uriRegexp);
		restMock.on(uriRegexp, action);
	}

	public <Req, Resp> Resp restRequest(String uri, Req request, Class<Resp> responseClass) {
		var port = serviceContainer.getMappedPort(8080);
		var restTemplate = new RestTemplate();
		var responseString = "";
		try {
			log.info("outbound request: >>> {}", uri);
			var responseEntity = restTemplate.getForEntity("http://localhost:" + port + uri, String.class);
			responseString = responseEntity.getBody();
			if (responseClass != String.class) {
				return new ObjectMapper().readValue(responseString, responseClass);
			} else {
				return (Resp) responseString;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			log.info("outbound request: <<< {}", responseString);
		}
	}
}
