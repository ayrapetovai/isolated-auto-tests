package com.example.testing.template.conf;

import com.example.testing.template.view.RestView;
import com.example.testing.util.DockerUtils;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ServiceTemplate implements ApplicationTemplate {

  private final String id;
  private final String imageName;
  @Getter
  private boolean isStatic = false;
  private boolean printLogs = false;
  private int servicePort = 8080;

  private GenericContainer<?> serviceContainer;

  private final Map<String, LazyGetter> env = new HashMap<>();

  private final List<Consumer<RestView>> initializers = new ArrayList<>();

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

  public ServiceTemplate printLogs(boolean printLogs) {
    this.printLogs = printLogs;
    return this;
  }

  public int getMapperServicePort() {
    return serviceContainer.getMappedPort(servicePort);
  }

  public LazyGetter getBaseUrl() {
    return () -> "http://host.docker.internal:" + serviceContainer.getMappedPort(servicePort) + "/";
  }

  public LazyGetter getSelfUrl() {
    return () -> {
      String host = "localhost";
      if (DockerUtils.runsInDockerContainer()) {
        host = "host.docker.internal";
      }
      return "http://" + host + ":" + serviceContainer.getMappedPort(servicePort) + "/";
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

    if (printLogs) {
      var logger = LoggerFactory.getLogger(id);
      serviceContainer.withLogConsumer(DockerUtils.createLogPrinter(logger));
    }

    serviceContainer.start();
    serviceContainer.waitingFor(Wait.forListeningPort());

    var view = new RestView(this);
    initializers.forEach(initializer ->
        initializer.accept(view));
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
    initializers.clear();
  }

  public ServiceTemplate init(Consumer<RestView> initializer) {
    initializers.add(initializer);
    return this;
  }
}
