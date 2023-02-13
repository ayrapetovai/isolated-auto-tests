package com.example.testing.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RandomPortAwareBean {

  @Autowired
  private ServletWebServerApplicationContext webServerAppCtxt;

  public Integer getSelfPort() {
    return webServerAppCtxt.getWebServer().getPort();
  }

}
