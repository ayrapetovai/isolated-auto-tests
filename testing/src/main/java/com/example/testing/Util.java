package com.example.testing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Util {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.registerModule(new JavaTimeModule());
  }

  public static String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T fromJson(String value, Class<T> result) {
    try {
      return objectMapper.readValue(value, result);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("failed to parse value: " + value, e);
    }
  }
}
