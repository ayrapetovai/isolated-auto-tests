package com.example.testing.template.conf;

import com.example.testing.RestMock;
import com.example.testing.util.RandomPortAwareBean;
import com.example.testing.util.StaticContextAccessor;
import lombok.Getter;

public class MockTemplate implements ApplicationTemplate {

  private final String id;
  @Getter
  private final boolean isStatic = true;
  private Integer selfPort;
  @Getter
  private RestMock restMock;

  public MockTemplate(String id) {
    this.id = id;
  }

  public LazyGetter getBaseUrl() {
    return () -> "http://host.docker.internal:" + this.selfPort + "/";
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void createAndAwait() {
    selfPort = StaticContextAccessor.getBean(RandomPortAwareBean.class).getSelfPort();
    restMock = StaticContextAccessor.getBean(RestMock.class);
  }

  @Override
  public void close() {
  }

  @Override
  public void finallyClose() {

  }
}
