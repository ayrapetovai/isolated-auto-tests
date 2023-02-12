package com.example.testing;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestConfigParameterResolver implements ParameterResolver {
  private static final TestEnvironment TEST_ENVIRONMENT = new TestEnvironment();

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    var param = parameterContext.getParameter();
    return param.getType() == TestEnvironment.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return TEST_ENVIRONMENT;
  }
}
