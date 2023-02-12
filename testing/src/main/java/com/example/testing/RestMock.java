package com.example.testing;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RestMock {

  private final Map<String,Function<RequestData, Object>> actions = new HashMap<>();
  private final List<Exception> fails = new ArrayList<>();

  public void on(String uriRegexp, Function<RequestData, Object> action) {
    actions.put(uriRegexp, action);
  }

  protected Object handle(String uri, RequestData requestData) {
    try {
      var keyList = actions.keySet()
          .stream()
          .filter(uriRegexp -> Pattern.compile(uriRegexp).matcher(uri).matches())
          .toList();
      if (keyList.isEmpty()) {
        throw new IllegalArgumentException("cannot find action for uri '" + uri + "', actions " + actions.keySet());
      }
      if (keyList.size() > 1) {
        throw new IllegalArgumentException("more then one action action is found for uri '" + uri + "', actions " + actions.keySet());
      }
      var key = keyList.get(0);
      var action = actions.get(key);
      return action.apply(requestData);
    } catch (Exception e) {
      fails.add(e);
    }
    return null;
  }
  public void close() {
    actions.clear();
    var errors = new ArrayList<>(fails);
    fails.clear();
    if (!errors.isEmpty()) {
      throw new RestMockHasFailedException(errors);
    }
  }
}
