package com.example.testing.template.view;

import com.example.testing.RequestData;
import com.example.testing.template.conf.MockTemplate;

import java.util.function.Function;

public class MockView {

  private final MockTemplate mockTemplate;

  public MockView(MockTemplate mockTemplate) {
    this.mockTemplate = mockTemplate;
  }

  public void mockRest(String uriRegexp, Function<RequestData, Object> action) {
//    log.info("mock rest endpoint `{}`", uriRegexp);
    mockTemplate.getRestMock().on(uriRegexp, action);
  }
}
