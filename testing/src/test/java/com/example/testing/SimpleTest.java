package com.example.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.util.StringUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@TestMethodOrder()
public class SimpleTest extends TestingApplicationTests {

  @Override
  public void setUpTestEnvironment(TestEnvironment testEnvironment) {
    testEnvironment.go = true;
  }

  /**
   * Checking:
   * `demo` service must give one of strings {Hello, Welcome, Cheers} on
   *   request to /greetings/{X}, where X is a number of greeting in {0, 1, 2}.
   */
  @ParameterizedTest
  @CsvSource({"0, Hello", "1, Welcome", "2, Cheers"})
  public void checkGreetingsHappyPass(int greetingNumber, String expectedGreeting) {
    var greeting = restRequest("/greetings/" + greetingNumber, null, String.class);
    assertEquals(expectedGreeting, greeting);
  }

  /**
   * Checking:
   * `demo` service must not give string on request to /greetings/{X},
   *   where X is NOT in {0, 1, 2}.
   */
  @Test
  public void checkGreetingsFails() {
    assertThrows(Exception.class, () -> restRequest("/greetings/3", null, String.class));
    assertThrows(Exception.class, () -> restRequest("/greetings/abc", null, String.class));
  }

  /**
   * Checking:
   *  1. `demo` service must have at lest one record in table `auto_test_user`.
   *  2. `demo` service must do request to /greetings/{X}, where X in {0, 1, 2}.
   *  3. `demo` service must give a response to request /greetings/{userId} with
   *    the name of a user with id == userId and greeting from response of /greetings/{X}.
   *  4. The /greeting/{userId} response must be of format "{GREETING}, {NAME}!".
   */
  @Test
  public void checkGreetingByUserId() {
    record User(int id, String name) { }
    var users = query("select id, name from auto_test_user",
        (rs, row) -> new User(rs.getInt("id"), rs.getString("name")));
    assertFalse(users.isEmpty());

    var firstRandomUser = users.stream().findFirst().get();
    var userId = firstRandomUser.id;
    var userName = firstRandomUser.name;
    assertTrue(StringUtils.isNotBlank(userName));

    var substitutionForGreeting = "HELLO";
    mockRest("/greetings/[0-2]{1}", requestData -> {
      assertEquals("GET", requestData.method());
      var uri = requestData.uri();
      var paramString = uri.substring(uri.lastIndexOf('/') + 1);
      var paramInt = Integer.valueOf(paramString);
      assertTrue(List.of(0, 1, 2).contains(paramInt));
      return substitutionForGreeting;
    });

    var expectedMessage = substitutionForGreeting + ", " + userName + "!";
    var message = restRequest("/greeting/" + userId, null, String.class);
    assertEquals(expectedMessage, message);
  }

}
