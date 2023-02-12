package com.example.testing.template;

import com.example.testing.RequestData;
import com.example.testing.RestMock;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static com.example.testing.Util.toJson;

@Slf4j
@ExtendWith(TestConfigParameterResolver.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public abstract class TestingApplicationTests {

	private static PostgreSQLContainer<?> postgreSQLContainer;
	private static JdbcTemplate jdbcTemplate;
	private static GenericContainer<?> serviceContainer;

	@Autowired
	private RestMock restMock;

	@LocalServerPort
	private Integer selfPort;

	@BeforeAll
	public static void beforeAll(TestEnvironment testEnvironment) {

	}

	@AfterAll
	public static void afterAll(TestEnvironment testEnvironment) {

	}

	@BeforeEach
	public final void beforeEach(TestEnvironment testEnvironment) {
		setUpTestEnvironment(testEnvironment);

		var postgresPort = 5432;
		postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
				.withDatabaseName("postgres")
				.withUsername("my_db_user")
				.withPassword("my_db_user_password")
				.withEnv("PGPORT", String.valueOf(postgresPort))
				.withExposedPorts(postgresPort);
		postgreSQLContainer.start();
		postgreSQLContainer.waitingFor(Wait.forListeningPort());

		var hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
		hikariConfig.setUsername(postgreSQLContainer.getUsername());
		hikariConfig.setPassword(postgreSQLContainer.getPassword());
		hikariConfig.setDriverClassName(postgreSQLContainer.getDriverClassName());
		var dataSource = new HikariDataSource(hikariConfig);
		jdbcTemplate = new JdbcTemplate(dataSource);

		var serviceContainerPort = 8080;
		serviceContainer = new GenericContainer<>("com.example/demo:latest")
				.withExposedPorts(serviceContainerPort)
				.withEnv("PORT", String.valueOf(serviceContainerPort))
				.withEnv("GREETINGS_URL", "http://host.docker.internal:" + selfPort + "/")
				.withEnv("JDBC_URL", "jdbc:postgresql://host.docker.internal:" + postgreSQLContainer.getMappedPort(postgresPort) + "/postgres")
				.withEnv("DB_USER", postgreSQLContainer.getUsername())
				.withEnv("DB_PASS", postgreSQLContainer.getPassword());
		serviceContainer.start();
		serviceContainer.waitingFor(Wait.forListeningPort());
	}

	@AfterEach
	public final void afterEach() {
		serviceContainer.stop();
		postgreSQLContainer.stop();
		restMock.close();
	}

	public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		log.info("query: >>> {}", sql);
		return jdbcTemplate.query(sql, (rs, row) -> {
			var mapResult = rowMapper.mapRow(rs, row);
			log.info("query: <<< {}", toJson(mapResult));
			return mapResult;
		});
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
			log.info("outbound request: <<< {}", responseString);
			if (responseClass != String.class) {
				return new ObjectMapper().readValue(responseString, responseClass);
			} else {
				return responseClass.cast(responseString);
			}
		} catch (IOException e) {
			log.info("outbound request: <<< failed: error {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public abstract void setUpTestEnvironment(TestEnvironment testEnvironment);
}
