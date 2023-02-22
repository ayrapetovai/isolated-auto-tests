package com.example.testing.template;

import com.example.testing.IsolatedAutoTest;
import com.example.testing.TestEnvironmentInitializer;
import com.example.testing.template.conf.MockTemplate;
import com.example.testing.template.conf.PostgresTemplate;
import com.example.testing.template.conf.ServiceTemplate;
import com.example.testing.template.view.DbView;
import com.example.testing.template.view.MockView;
import com.example.testing.template.view.RestView;
import org.junit.jupiter.api.extension.*;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Modifier;

public class TestEnvironmentLifeCycleController implements ParameterResolver, BeforeEachCallback, AfterEachCallback {
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

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    var testInstance = context.getTestInstance().orElseThrow();
    if (testInstance.getClass().getDeclaredAnnotation(IsolatedAutoTest.class) == null) {
      return;
    }

    var testEnvironmentInitializerCandidates = ReflectionUtils.getUniqueDeclaredMethods(
        testInstance.getClass(),
        method -> method.getAnnotation(TestEnvironmentInitializer.class) != null
    );
    if (testEnvironmentInitializerCandidates.length > 1) {
      throw new IllegalStateException("Only one method can be annotated as @TestEnvironmentInitializer");
    }
    if (testEnvironmentInitializerCandidates.length == 0) {
      throw new IllegalStateException("on initializing method declared: create @TestEnvironmentInitializer public void init(TestEnvironment env) {...} in class " +
          testInstance.getClass().getCanonicalName());
    }
    var testEnvironmentInitializer = testEnvironmentInitializerCandidates[0];
    if (testEnvironmentInitializer.getParameterCount() != 1) {
      throw new IllegalStateException("no environment passed to initialing method: add a `TestEnvironment env` parameter to method `" + testEnvironmentInitializer.getName()
          + "` in class `" + testInstance.getClass().getCanonicalName() + "`");
    }
    if ((testEnvironmentInitializer.getModifiers() & Modifier.STATIC) != 0) {
      ReflectionUtils.invokeMethod(testEnvironmentInitializer, null, TEST_ENVIRONMENT);
    } else {
      ReflectionUtils.invokeMethod(testEnvironmentInitializer, testInstance, TEST_ENVIRONMENT);
    }
    if (TEST_ENVIRONMENT.registrantsSize() < 1) {
      throw new IllegalStateException("no providers was declared at TestEnvironment in method `" +
          testEnvironmentInitializer.getName() + "` of class `" + testInstance.getClass().getCanonicalName() + "`");
    }
    TEST_ENVIRONMENT.createAndAwait();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    TEST_ENVIRONMENT.close();
  }
}
