//package com.example.testing;
//
//import org.junit.jupiter.api.extension.ExtensionContext;
//import org.junit.jupiter.api.extension.ParameterContext;
//import org.junit.jupiter.api.extension.ParameterResolutionException;
//import org.junit.jupiter.api.extension.ParameterResolver;
//
//public class TestConfigParameterResolver implements ParameterResolver {
//
//  @Override
//  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
//    var param = parameterContext.getParameter();
//    return param.getType() == TestConfig.class;
//  }
//
//  @Override
//  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
//    return new TestConfig();
//  }
//}
