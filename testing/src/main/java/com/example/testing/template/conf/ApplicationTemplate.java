package com.example.testing.template.conf;

public interface ApplicationTemplate {
  String getId();
  void createAndAwait();
  boolean isStatic();
  void close();
  void finallyClose();
}
