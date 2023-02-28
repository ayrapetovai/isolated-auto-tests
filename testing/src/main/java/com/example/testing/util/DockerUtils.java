package com.example.testing.util;

import org.slf4j.Logger;
import org.testcontainers.containers.output.OutputFrame;

import java.util.function.Consumer;

public class DockerUtils {
  public static boolean runsInDockerContainer() {
    return false; // TODO
  }

  public static Consumer<OutputFrame> createLogPrinter(Logger logger) {
    return of -> {
      var bytes = of.getBytes();
      if (bytes != null && bytes.length > 0) {
        var length = bytes[bytes.length - 1] == '\n' ? bytes.length - 1 : bytes.length;
        logger.info("{}", new String(bytes, 0, length));
      }
    };
  }
}
