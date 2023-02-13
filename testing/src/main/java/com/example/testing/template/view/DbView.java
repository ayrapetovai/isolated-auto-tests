package com.example.testing.template.view;

import com.example.testing.template.conf.PostgresTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class DbView {
  private final PostgresTemplate postgresTemplate;

  public DbView(PostgresTemplate postgresTemplate) {
    this.postgresTemplate = postgresTemplate;
  }

  public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
    var jdbcTemplate = postgresTemplate.getJdbcTemplate();
//    log.info("query: >>> {}", sql);
    return jdbcTemplate.query(sql, (rs, row) -> {
      var mapResult = rowMapper.mapRow(rs, row);
//      log.info("query: <<< {}", toJson(mapResult));
      return mapResult;
    });
  }
}
