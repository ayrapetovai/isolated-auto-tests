package com.example.testing.template.conf;

import com.example.testing.util.DockerUtils;
import lombok.Getter;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.HashMap;
import java.util.Map;

public class ServiceTemplate implements ApplicationTemplate {

  private final String id;
  private final String imageName;
  @Getter
  private boolean isStatic = false;
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

  public ServiceTemplate isStatic(boolean isStatic) {
    this.isStatic = isStatic;
    return this;
  }

  public int getMapperServicePort() {
    return serviceContainer.getMappedPort(servicePort);
  }

  public LazyGetter getBaseUrl() {
    return () -> {
      String host = "localhost";
      if (DockerUtils.runsInDockerContainer()) {
        host = "host.docker.internal";
      }
      return "http://" + host + ":" + serviceContainer.getMappedPort(servicePort);
    };
  }

  @Override
  public void createAndAwait() {
    if (isStatic && serviceContainer != null && serviceContainer.isCreated()) {
      return;
    }

    serviceContainer = new GenericContainer<>(imageName)
        .withExposedPorts(servicePort)
        .withEnv("PORT", String.valueOf(servicePort));
    env.forEach((name, lazyGetter) -> serviceContainer.withEnv(name, lazyGetter.get()));
    serviceContainer.start();
    serviceContainer.waitingFor(Wait.forListeningPort());
  }

  @Override
  public void close() {
    if (!isStatic && serviceContainer != null && serviceContainer.isCreated()) {
      serviceContainer.close();
      serviceContainer = null;
    }
  }

  @Override
  public void finallyClose() {
    if (serviceContainer != null && serviceContainer.isCreated()) {
      serviceContainer.close();
    }
  }
}
