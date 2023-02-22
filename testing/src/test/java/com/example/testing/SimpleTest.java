package com.example.testing;

import com.example.testing.template.TestEnvironment;
import com.example.testing.template.TestTemplate;
import com.example.testing.template.view.DbView;
import com.example.testing.template.view.MockView;
import com.example.testing.template.view.RestView;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleTest extends TestTemplate {

  @Override
  public void setUpTestEnvironment(TestEnvironment testEnvironment) {
    var db = testEnvironment.postgres("db", "postgres:latest");
    var mock = testEnvironment.mock("mock");
    testEnvironment.service("testTarget", "com.example/demo:latest")
      .env("LOG_LEVEL", "DEBUG")
      .env("GREETINGS_URL", mock.getSelfUrl())
      .env("JDBC_URL", db.getJdbcUrl())
      .env("DB_USER", db.getUser())
      .env("DB_PASS", db.getPassword());
  }

  /**
   * Checking:
   * `demo` service must give one of strings {Hello, Welcome, Cheers} on
   *   request to /greetings/{X}, where X is a number of greeting in {0, 1, 2}.
   */
  @Order(1)
  @ParameterizedTest
  @CsvSource({"0, Hello", "1, Welcome", "2, Cheers"})
  public void checkGreetingsHappyPass(int greetingNumber, String expectedGreeting, RestView testTarget) {
    var greeting = testTarget.getRestTemplate()
        .getForObject("/greetings/" + greetingNumber, String.class);
    assertEquals(expectedGreeting, greeting);
  }

  /**
   * Checking:
   * `demo` service must not give string on request to /greetings/{X},
   *   where X is NOT in {0, 1, 2}.
   */
  @Order(2)
  @Test
  public void checkGreetingsFails(RestView testTarget) {
    var restTemplate = testTarget.getRestTemplate(false);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, restTemplate.getForEntity("/greetings/-1", String.class).getStatusCode());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, restTemplate.getForEntity("/greetings/3", String.class).getStatusCode());
    assertEquals(HttpStatus.BAD_REQUEST, restTemplate.getForEntity("/greetings/abc", String.class).getStatusCode());
  }

  /**
   * Checking:
   *  1. `demo` service must have at lest one record in table `auto_test_user`.
   *  2. `demo` service must do request to /greetings/{X}, where X in {0, 1, 2}.
   *  3. `demo` service must give a response to request /greetings/{userId} with
   *    the name of a user with id == userId and greeting from response of /greetings/{X}.
   *  4. The /greeting/{userId} response must be of format "{GREETING}, {NAME}!".
   */
  @Order(3)
  @Test
  public void checkGreetingByUserId(RestView testTarget, DbView db, MockView mock) {
    record User(int id, String name) { }
    var users = db.getJdbcTemplate()
        .query("select id, name from auto_test_user",
            (rs, row) -> new User(rs.getInt("id"), rs.getString("name")));
    assertFalse(users.isEmpty());

    var firstRandomUser = users.stream().findFirst().get();
    var userId = firstRandomUser.id;
    var userName = firstRandomUser.name;
    assertTrue(StringUtils.isNotBlank(userName));

    var substitutionForGreeting = "HELLO";
    mock.mockRest("/greetings/[0-2]{1}", requestData -> {
      assertEquals("GET", requestData.method());
      var uri = requestData.uri();
      var paramString = uri.substring(uri.lastIndexOf('/') + 1);
      var paramInt = Integer.valueOf(paramString);
      assertTrue(List.of(0, 1, 2).contains(paramInt));
      return substitutionForGreeting;
    });

    var expectedMessage = substitutionForGreeting + ", " + userName + "!";
    var message = testTarget.getRestTemplate()
        .getForObject("/greeting/" + userId, String.class);
    assertEquals(expectedMessage, message);
  }

}
