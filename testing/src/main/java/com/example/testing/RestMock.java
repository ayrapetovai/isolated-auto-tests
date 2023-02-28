package com.example.testing;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestMock {

  record RequestHandler(int allowedCalls, Function<RequestData, Object> action, List<Exception> fails){}
  private final Map<String, RequestHandler> requestHandlers = new HashMap<>();
  private final Map<String, Integer> registeredCalls = new ConcurrentHashMap<>();

  public void on(String uriRegexp, int allowedCalls, Function<RequestData, Object> action) {
    requestHandlers.put(uriRegexp, new RequestHandler(allowedCalls, action, new ArrayList<>()));
  }

  protected Object handle(String uri, RequestData requestData) {
    RequestHandler requestHandler = null;
    try {
      var keyList = requestHandlers.keySet()
          .stream()
          .filter(uriRegexp -> Pattern.compile(uriRegexp).matcher(uri).matches())
          .toList();
      if (keyList.isEmpty()) {
        throw new IllegalArgumentException("cannot find action for uri '" + uri + "', actions " + requestHandlers.keySet());
      }
      if (keyList.size() > 1) {
        throw new IllegalArgumentException("more then one action action is found for uri '" + uri + "', actions " + requestHandlers.keySet());
      }
      var key = keyList.get(0);
      requestHandler = requestHandlers.get(key);
      synchronized (registeredCalls) {
        int currentCount = registeredCalls.computeIfAbsent(key, k -> 0);
        if (currentCount + 1 > requestHandler.allowedCalls()) {
          requestHandler.fails.add(new IllegalStateException("too many calls, allowed " + requestHandler.allowedCalls() + ", but got " + (currentCount + 1)));
        }
        try {
          return requestHandler.action.apply(requestData);
        } finally {
          registeredCalls.put(key, currentCount + 1);
        }
      }
    } catch (Exception e) {
      if (requestHandler != null) {
        requestHandler.fails.add(e);
      }
    }
    return null;
  }

  public void clear() {
    try {
      var fails = requestHandlers.values().stream()
          .map(RequestHandler::fails)
          .flatMap(List::stream)
          .collect(Collectors.toList());
      if (!fails.isEmpty()) {
        throw new AggregateException("closing " + this.getClass().getSimpleName() + " failed with errors", fails);
      }
    } finally {
      requestHandlers.clear();
      registeredCalls.clear();
    }
  }

  public void waitFor(String uriRegexp, int count, Duration duration) {
    var requestHandler = requestHandlers.get(uriRegexp);
    if (requestHandler == null) {
      throw new IllegalArgumentException("uri " + uriRegexp + " was not registered");
    }

    var start = LocalDateTime.now();
    while (true) {
      if (registeredCalls.computeIfAbsent(uriRegexp, k -> 0) >= count) {
        if (!requestHandler.fails.isEmpty()) {
          throw new AggregateException("waiting for " + uriRegexp + " failed with errors", requestHandler.fails);
        }
        break;
      }
      if (!Duration.ZERO.equals(duration) && duration != null) {
        if (LocalDateTime.now().isAfter(start.plus(duration))) {
          throw new RuntimeException(new TimeoutException("waiting for requests for uri " + uriRegexp + " longer then " + duration));
        }
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
