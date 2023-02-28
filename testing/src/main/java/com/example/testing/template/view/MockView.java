package com.example.testing.template.view;

import com.example.testing.RequestData;
import com.example.testing.template.conf.MockTemplate;

import java.time.Duration;
import java.util.function.Function;

public class MockView {

  private final MockTemplate mockTemplate;

  public MockView(MockTemplate mockTemplate) {
    this.mockTemplate = mockTemplate;
  }

  // TODO muck actual response, not only response body?
  public void mockEndpoint(String uriRegexp, int allowedCalls, Function<RequestData, Object> action) {
//    log.info("mock rest endpoint `{}`", uriRegexp);
    mockTemplate.getRestMock().on(uriRegexp, allowedCalls, action);
  }

  public void mockEndpoint(String uriRegexp, Function<RequestData, Object> action) {
//    log.info("mock rest endpoint `{}`", uriRegexp);
    mockTemplate.getRestMock().on(uriRegexp, Integer.MAX_VALUE, action);
  }

  public void waitFor(String uriRegexp, int count, Duration duration) {
    mockTemplate.getRestMock().waitFor(uriRegexp, count, duration);
  }

  public String getBaseUrl() {
    return mockTemplate.getBaseUrl().get();
  }
}
