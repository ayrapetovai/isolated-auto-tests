package com.example.testing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

public class RestMockHasFailedException extends RuntimeException {
  private final List<Exception> fails;

  public RestMockHasFailedException(List<Exception> fails) {
    super("rest mock error list");
    this.fails = Collections.unmodifiableList(fails);
  }

  public String toString() {
    String s = getClass().getName();
    String message = getLocalizedMessage();
    var errorDescriptions = new StringBuilder();
    errorDescriptions.append(s).append(": ").append(message).append("\n");

    fails.forEach(e -> {
      var printer = new PrintStream(new ByteArrayOutputStream());
      e.printStackTrace(printer);
      errorDescriptions.append(e.getMessage());
      errorDescriptions.append(printer);
    });

    return errorDescriptions.toString();
  }
}
