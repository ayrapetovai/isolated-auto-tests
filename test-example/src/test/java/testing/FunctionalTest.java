package testing;

import com.example.testing.IsolatedAutoTest;
import com.example.testing.TestEnvironmentInitializer;
import com.example.testing.template.TestEnvironment;
import com.example.testing.template.view.DbView;
import com.example.testing.template.view.MockView;
import com.example.testing.template.view.RestView;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@IsolatedAutoTest
public class FunctionalTest {
  @TestEnvironmentInitializer
  public void initTestEnv(TestEnvironment testEnvironment) {
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
