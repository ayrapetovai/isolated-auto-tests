package com.example.testing.template;

import com.example.testing.template.conf.ApplicationTemplate;
import com.example.testing.template.conf.MockTemplate;
import com.example.testing.template.conf.PostgresTemplate;
import com.example.testing.template.conf.ServiceTemplate;
import com.example.testing.template.view.DbView;
import com.example.testing.template.view.MockView;
import com.example.testing.template.view.RestView;

import java.util.*;

public class TestEnvironment {

  // order is taken to account
  private final List<ApplicationTemplate> registrants = new ArrayList<>();
  private final Map<String, ApplicationTemplate> registrantsCache = new HashMap<>();

  public ApplicationTemplate getById(String name) {
    return registrants.stream().filter(r -> r.getId().equals(name)).findFirst().orElse(null);
  }

  public ApplicationTemplate getByType(Class<?> type) {
    return registrants.stream().filter(r -> r instanceof PostgresTemplate && type == DbView.class ||
        r instanceof MockTemplate && type == MockView.class ||
        r instanceof ServiceTemplate && type == RestView.class).findFirst().orElse(null);
  }

  public PostgresTemplate postgres(String id, String imageName) {
    if (registrantsCache.containsKey(id)) {
      return (PostgresTemplate) registrantsCache.get(id);
    }
    var app = new PostgresTemplate(id, imageName);
    registrants.add(app);
    return app;
  }

  public MockTemplate mock(String id) {
    if (registrantsCache.containsKey(id)) {
      return (MockTemplate) registrantsCache.get(id);
    }
    var app = new MockTemplate(id);
    registrants.add(app);
    return app;
  }

  public ServiceTemplate service(String id, String imageName) {
    if (registrantsCache.containsKey(id)) {
      return (ServiceTemplate) registrantsCache.get(id);
    }
    var app = new ServiceTemplate(id, imageName);
    registrants.add(app);
    return app;
  }

  void createAndAwait() {
    for (var registrant : registrants) {
      registrant.createAndAwait();
    }
  }

  void close() {
    var registrantsReversed = new ArrayList<>(registrants);
    Collections.reverse(registrantsReversed);

    for (var registrant : registrantsReversed) {
      registrant.close();
    }

    registrants.clear();
    registrantsCache.clear();
  }

}
