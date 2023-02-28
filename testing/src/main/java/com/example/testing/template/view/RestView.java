package com.example.testing.template.view;

import com.example.testing.template.conf.ServiceTemplate;
import com.example.testing.util.NoThrowErrorHandler;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

public class RestView {

  private final ServiceTemplate serviceTemplate;

  public RestView(ServiceTemplate serviceTemplate) {
    this.serviceTemplate = serviceTemplate;
  }

  public RestTemplate getRestTemplate(boolean throwOnHttpError) {
    var host = serviceTemplate.getSelfUrl().get();
    var restTemplate = new RestTemplate();
    restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(host));
    restTemplate.setInterceptors(List.of((request, body, execution) -> {
      var givenHost = request.getURI().getHost() + ":" + (request.getURI().getPort() > 0? request.getURI().getPort(): 80);
      var targetUri = URI.create(host);
      var targetHost = targetUri.getHost() + ":" + targetUri.getPort();
      if (!targetHost.equals(givenHost)) {
        var id = serviceTemplate.getId();
        throw new IllegalStateException(
            "this rest template allowed to send request only to service " + id + " at " + targetHost + ", given " + givenHost
        );
      }
      return execution.execute(request, body);
    }));
    if (!throwOnHttpError) {
      restTemplate.setErrorHandler(new NoThrowErrorHandler());
    }
    return restTemplate;
  }

  public RestTemplate getRestTemplate() {
    return getRestTemplate(true);
  }

  public String getBaseUrl() {
    return serviceTemplate.getBaseUrl().get();
  }
}
