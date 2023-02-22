package com.example.testing.util;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.net.URI;

public class NoThrowErrorHandler implements ResponseErrorHandler {
  @Override
  public boolean hasError(ClientHttpResponse response) {
    return false;
  }

  @Override
  public void handleError(ClientHttpResponse response) {

  }

  @Override
  public void handleError(URI url, HttpMethod method, ClientHttpResponse response) {
  }
}
