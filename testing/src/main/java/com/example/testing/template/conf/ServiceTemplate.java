package com.example.testing.template.conf;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.HashMap;
import java.util.Map;

public class ServiceTemplate implements ApplicationTemplate {

  private final String id;
  private final String imageName;

  private int servicePort = 8080;

  private GenericContainer<?> serviceContainer;

  private final Map<String, LazyGetter> env = new HashMap<>();

  public ServiceTemplate(String id, String imageName) {
    this.id = id;
    this.imageName = imageName;
  }

  public ServiceTemplate env(String name, String value) {
    env.put(name, () -> value);
    return this;
  }

  public ServiceTemplate env(String name, LazyGetter lazyGetter) {
    env.put(name, lazyGetter);
    return this;
  }

  public ServiceTemplate port(int port) {
    this.servicePort = port;
    return this;
  }

  @Override
  public String getId() {
    return id;
  }

  public int getMapperServicePort() {
    return serviceContainer.getMappedPort(servicePort);
  }

  @Override
  public void createAndAwait() {
    serviceContainer = new GenericContainer<>(imageName)
        .withExposedPorts(servicePort)
        .withEnv("PORT", String.valueOf(servicePort));
    env.forEach((name, lazyGetter) -> {
      serviceContainer.withEnv(name, lazyGetter.get());
    });
    serviceContainer.start();
    serviceContainer.waitingFor(Wait.forListeningPort());
  }

  @Override
  public void close() {
    serviceContainer.close();
  }
}