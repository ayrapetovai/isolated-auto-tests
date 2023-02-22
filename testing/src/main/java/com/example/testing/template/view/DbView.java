package com.example.testing.template.view;

import com.example.testing.template.conf.PostgresTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

public class DbView {
  private final PostgresTemplate postgresTemplate;

  public DbView(PostgresTemplate postgresTemplate) {
    this.postgresTemplate = postgresTemplate;
  }

  public JdbcTemplate getJdbcTemplate() {
    return postgresTemplate.getJdbcTemplate();
  }
}
