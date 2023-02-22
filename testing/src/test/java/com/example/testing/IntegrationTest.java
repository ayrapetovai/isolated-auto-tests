package com.example.testing;

import com.example.testing.template.TestEnvironment;
import com.example.testing.template.view.DbView;
import com.example.testing.template.view.RestView;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@IsolatedAutoTest
public class IntegrationTest {

  private static final String DATABASE_TAG = "db";
  private static final String THIRD_PARTY_SERVICE_TAG = "tps";
  private static final String TEST_TARGET_SERVICE_TAG = "tts";

  @TestEnvironmentInitializer
  public void init(TestEnvironment testEnvironment) {
    var db = testEnvironment.postgres(DATABASE_TAG, "postgres:latest");
    var thirdPartyService = testEnvironment.service(THIRD_PARTY_SERVICE_TAG, "com.example/demo:latest")
        .env("GREETINGS_URL", "<no-url-unused>")
        .env("JDBC_URL", db.getJdbcUrl())
        .env("DB_USER", db.getUser())
        .env("DB_PASS", db.getPassword());
    testEnvironment.service(TEST_TARGET_SERVICE_TAG, "com.example/demo:latest")
        .env("GREETINGS_URL", thirdPartyService.getBaseUrl())
        .env("JDBC_URL", db.getJdbcUrl())
        .env("DB_USER", db.getUser())
        .env("DB_PASS", db.getPassword());
  }

  @Test
  public void checkIntegrationWithThirdParty(
      @EnvironmentView(id = DATABASE_TAG) DbView databaseView,
      @EnvironmentView(id = TEST_TARGET_SERVICE_TAG) RestView underTest,
      @EnvironmentView(id = THIRD_PARTY_SERVICE_TAG) RestView thirdParty
  ) {
    var someFromThirdParty = thirdParty.getRestTemplate()
        .getForObject("/greetings/1", String.class);
    assertNotNull(someFromThirdParty);

    var userId = databaseView.getJdbcTemplate()
        .queryForObject("select id from auto_test_user limit 1", Integer.class);
    assertNotNull(userId);

    var userName = databaseView.getJdbcTemplate()
        .queryForObject("select name from auto_test_user where id = " + userId, String.class);
    assertTrue(StringUtils.isNotBlank(userName));

    var actualGreeting = underTest.getRestTemplate()
        .getForObject("/greeting/" + userId, String.class);

    assertNotNull(actualGreeting);
    assertTrue(actualGreeting.contains(userName));
  }

}
