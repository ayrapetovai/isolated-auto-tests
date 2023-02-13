package com.example.testing.template.conf;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class PostgresTemplate implements ApplicationTemplate {

  private final String id;
  private final String imageName;
  private PostgreSQLContainer<?> postgreSQLContainer;
  @Getter
  private JdbcTemplate jdbcTemplate;
  private int postgresPort = 5432;
  private String postgresDatabase = "postgres";

  public PostgresTemplate(String id, String imageName) {
    this.id = id;
    this.imageName = imageName;
  }

  public LazyGetter getJdbcUrl() {
    return () -> "jdbc:postgresql://host.docker.internal:" + this.postgreSQLContainer.getMappedPort(postgresPort) + "/" + postgresDatabase;
  }

  public LazyGetter getUser() {
    return () -> this.postgreSQLContainer.getUsername();
  }

  public LazyGetter getPassword() {
    return () -> this.postgreSQLContainer.getPassword();
  }

  public PostgresTemplate port(int port) {
    this.postgresPort = port;
    return this;
  }

  public PostgresTemplate database(String database) {
    this.postgresDatabase = database;
    return this;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void createAndAwait() {
    postgreSQLContainer = new PostgreSQLContainer<>(imageName)
        .withDatabaseName(postgresDatabase)
        .withUsername("my_db_user")
        .withPassword("my_db_user_password")
        .withEnv("PGPORT", String.valueOf(postgresPort)) // TODO: this line does not anything
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
  }

  @Override
  public void close() {
    postgreSQLContainer.close();
  }
}
