package com.example.testing.template;

import com.example.testing.template.conf.MockTemplate;
import com.example.testing.template.conf.PostgresTemplate;
import com.example.testing.template.conf.ServiceTemplate;
import com.example.testing.template.view.DbView;
import com.example.testing.template.view.MockView;
import com.example.testing.template.view.RestView;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestConfigParameterResolver implements ParameterResolver {
  private static final TestEnvironment TEST_ENVIRONMENT = new TestEnvironment();

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    var param = parameterContext.getParameter();
    return
        param.getType() == TestEnvironment.class ||
            param.getType() == RestView.class ||
            param.getType() == DbView.class ||
            param.getType() == MockView.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    var param = parameterContext.getParameter();
    if (param.getType() == TestEnvironment.class) {
      return TEST_ENVIRONMENT;
    }
    var applicationTemplate = TEST_ENVIRONMENT.getById(param.getName());
    if (applicationTemplate == null) {
      applicationTemplate = TEST_ENVIRONMENT.getByType(param.getType());
    }
    if (applicationTemplate != null) {
      if (param.getType() == RestView.class) {
        return new RestView((ServiceTemplate) applicationTemplate);
      } else if (param.getType() == DbView.class) {
        return new DbView((PostgresTemplate) applicationTemplate);
      } else if (param.getType() == MockView.class) {
        return new MockView((MockTemplate) applicationTemplate);
      }
    }

    return null;
  }
}
