package com.example.testing.template.conf;

import com.example.testing.RestMock;
import com.example.testing.template.view.MockView;
import com.example.testing.util.RandomPortAwareBean;
import com.example.testing.util.StaticContextAccessor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MockTemplate implements ApplicationTemplate {

  private final String id;
  @Getter
  private final boolean isStatic = false;
  private Integer selfPort;
  @Getter
  private RestMock restMock;

  private final List<Consumer<MockView>> initializers = new ArrayList<>();

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
    var view = new MockView(this);
    initializers.forEach(initializer ->
        initializer.accept(view));
  }

  @Override
  public void close() {
    restMock.clear();
  }

  @Override
  public void finallyClose() {
    restMock.clear();
    initializers.clear();
  }

  public MockTemplate init(Consumer<MockView> initializer) {
    initializers.add(initializer);
    return this;
  }
}
