package testing;

import com.example.testing.IsolatedAutoTest;
import com.example.testing.TestEnvironmentInitializer;
import com.example.testing.template.TestEnvironment;
import com.example.testing.template.view.DbView;
import com.example.testing.template.view.RestView;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@IsolatedAutoTest
public class ApiTest {

  @TestEnvironmentInitializer
  public void initTestEnv(TestEnvironment testEnvironment) {
    var db = testEnvironment.postgres("db", "postgres:latest");
    var mock = testEnvironment.mock("mock");
    testEnvironment.service("testTarget", "com.example/demo:latest")
        .env("LOG_LEVEL", "DEBUG")
        .env("GREETINGS_URL", mock.getBaseUrl())
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

  @Order(3)
  @Test
  public void checkIfDbContainsUsers(DbView db) {
    record User(int id, String name) { }
    var users = db.getJdbcTemplate()
        .query("select id, name from auto_test_user",
            (rs, row) -> new User(rs.getInt("id"), rs.getString("name")));
    assertFalse(users.isEmpty());
  }

}
