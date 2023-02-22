package com.example.testing.template.view;

import com.example.testing.template.conf.ServiceTemplate;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

public class RestView {

  private final ServiceTemplate serviceTemplate;

  public RestView(ServiceTemplate serviceTemplate) {
    this.serviceTemplate = serviceTemplate;
  }

  public <Req, Resp> Resp restRequest(String uri, Req request, Class<Resp> responseClass) {
    var host = serviceTemplate.getBaseUrl();
    var restTemplate = new RestTemplate();
    restTemplate.setUriTemplateHandler(new RootUriTemplateHandler(host.get()));
    var responseString = "";
    try {
//      log.info("outbound request: >>> {}", uri);
      var responseEntity = restTemplate.getForEntity(uri, String.class);
      responseString = responseEntity.getBody();
//      log.info("outbound request: <<< {}", responseString);
      if (responseClass != String.class) {
        return new ObjectMapper().readValue(responseString, responseClass);
      } else {
        return responseClass.cast(responseString);
      }
    } catch (Exception e) {
//      log.info("outbound request: <<< failed: error {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
