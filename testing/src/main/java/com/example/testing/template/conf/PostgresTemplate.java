package com.example.testing.template.conf;

import com.example.testing.template.view.DbView;
import com.example.testing.util.DockerUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PostgresTemplate implements ApplicationTemplate {

  private final String id;
  private final String imageName;
  @Getter
  private boolean isStatic = false;
  private boolean printLogs = false;
  private PostgreSQLContainer<?> postgreSQLContainer;
  @Getter
  private JdbcTemplate jdbcTemplate;
  private int postgresPort = 5432;
  private String postgresDatabase = "postgres";
  private String databaseUser = "my_db_user";
  private String databaseUserPassword = "my_db_user_password";
  private final List<Consumer<DbView>> initializers = new ArrayList<>();

  public PostgresTemplate(String id, String imageName) {
    this.id = id;
    this.imageName = imageName;
  }

  public PostgresTemplate isStatic(boolean isStatic) {
    this.isStatic = isStatic;
    return this;
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

  public PostgresTemplate user(String user) {
    this.databaseUser = user;
    return this;
  }

  public PostgresTemplate password(String password) {
    this.databaseUserPassword = password;
    return this;
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

  public PostgresTemplate printLogs(boolean printLogs) {
    this.printLogs = printLogs;
    return this;
  }

  @Override
  public void createAndAwait() {
    if (isStatic && postgreSQLContainer != null && postgreSQLContainer.isCreated()) {
      return;
    }

    postgreSQLContainer = new PostgreSQLContainer<>(imageName)
        .withDatabaseName(postgresDatabase)
        .withUsername(databaseUser)
        .withPassword(databaseUserPassword)
        .withEnv("PGPORT", String.valueOf(postgresPort)) // TODO: this line does not anything
        .withExposedPorts(postgresPort);

    if (printLogs) {
      var logger = LoggerFactory.getLogger(id);
      postgreSQLContainer.withLogConsumer(DockerUtils.createLogPrinter(logger));
    }

    postgreSQLContainer.start();
    postgreSQLContainer.waitingFor(Wait.forListeningPort());

    var hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
    hikariConfig.setUsername(postgreSQLContainer.getUsername());
    hikariConfig.setPassword(postgreSQLContainer.getPassword());
    hikariConfig.setDriverClassName(postgreSQLContainer.getDriverClassName());
    var dataSource = new HikariDataSource(hikariConfig);
    jdbcTemplate = new JdbcTemplate(dataSource);

    var view = new DbView(this);
    initializers.forEach(initializer ->
        initializer.accept(view));
  }

  @Override
  public void close() {
    if (!isStatic && postgreSQLContainer != null && postgreSQLContainer.isCreated()) {
      postgreSQLContainer.close();
      postgreSQLContainer = null;
    }
  }

  @Override
  public void finallyClose() {
    if (postgreSQLContainer != null && postgreSQLContainer.isCreated()) {
      postgreSQLContainer.close();
    }
    initializers.clear();
  }

  public PostgresTemplate init(Consumer<DbView> initializer) {
    initializers.add(initializer);
    return this;
  }
}
