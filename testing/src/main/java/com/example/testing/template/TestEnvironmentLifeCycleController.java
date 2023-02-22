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
import java.util.HashMap;
import java.util.Map;

public class TestEnvironmentLifeCycleController implements ParameterResolver, BeforeEachCallback, AfterEachCallback, AfterAllCallback {
  private static final Map<Object, TestEnvironment> environmentsByTestClasses = new HashMap<>();

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
    var testClass = extensionContext.getTestClass().orElseThrow();
    if (param.getType() == TestEnvironment.class) {
      return environmentsByTestClasses.get(testClass);
    }
    var testEnvironment = environmentsByTestClasses.get(testClass);
    var applicationTemplate = testEnvironment.getById(param.getName());
    if (applicationTemplate == null) {
      applicationTemplate = testEnvironment.getByType(param.getType());
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
  public void beforeEach(ExtensionContext context) {
    var testInstance = context.getTestInstance().orElseThrow();
    if (testInstance.getClass().getDeclaredAnnotation(IsolatedAutoTest.class) == null) {
      return;
    }

    var testClass = testInstance.getClass();
    var testEnvironment = environmentsByTestClasses.computeIfAbsent(testClass, c -> new TestEnvironment());

    var testEnvironmentInitializerCandidates = ReflectionUtils.getUniqueDeclaredMethods(
        testClass,
        method -> method.getAnnotation(TestEnvironmentInitializer.class) != null
    );
    if (testEnvironmentInitializerCandidates.length > 1) {
      throw new IllegalStateException("Only one method can be annotated as @TestEnvironmentInitializer");
    }
    if (testEnvironmentInitializerCandidates.length == 0) {
      throw new IllegalStateException("on initializing method declared: create @TestEnvironmentInitializer public void init(TestEnvironment env) {...} in class " +
          testClass.getCanonicalName());
    }
    var testEnvironmentInitializer = testEnvironmentInitializerCandidates[0];
    if (testEnvironmentInitializer.getParameterCount() != 1) {
      throw new IllegalStateException("no environment passed to initialing method: add a `TestEnvironment env` parameter to method `" + testEnvironmentInitializer.getName()
          + "` in class `" + testClass.getCanonicalName() + "`");
    }
    if (testEnvironment.registrantsSize() < 1) {
      if ((testEnvironmentInitializer.getModifiers() & Modifier.STATIC) != 0) {
        ReflectionUtils.invokeMethod(testEnvironmentInitializer, null, testEnvironment);
      } else {
        ReflectionUtils.invokeMethod(testEnvironmentInitializer, testInstance, testEnvironment);
      }
    }
    if (testEnvironment.registrantsSize() < 1) {
      throw new IllegalStateException("no providers was declared at TestEnvironment in method `" +
          testEnvironmentInitializer.getName() + "` of class `" + testClass.getCanonicalName() + "`");
    }
    testEnvironment.createAndAwait();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    var testInstance = context.getTestInstance().orElseThrow();
    if (testInstance.getClass().getDeclaredAnnotation(IsolatedAutoTest.class) == null) {
      return;
    }

    var testClass = testInstance.getClass();
    var testEnvironment = environmentsByTestClasses.get(testClass);
    testEnvironment.closeNonStatic();
  }

  @Override
  public void afterAll(ExtensionContext context) {
    var testClass = context.getTestClass().orElseThrow();
    if (testClass.getDeclaredAnnotation(IsolatedAutoTest.class) == null) {
      return;
    }
    var testEnvironment = environmentsByTestClasses.get(testClass);
    testEnvironment.closeAll();
    environmentsByTestClasses.put(testClass, null);
  }

}
