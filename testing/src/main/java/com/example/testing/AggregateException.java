package com.example.testing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AggregateException extends RuntimeException {

  private final List<? extends Throwable> fails;

  public AggregateException(String message, List<? extends Throwable> fails) {
    super(message);
    this.fails = new ArrayList<>(fails);
  }

  public AggregateException(List<? extends Throwable> fails) {
    this.fails = new ArrayList<>(fails);
  }

  @Override
  public String getMessage() {
    return super.getMessage() + ": " + fails.stream().map(Throwable::getMessage)
        .collect(Collectors.joining(";"));
  }

  @Override
  public String getLocalizedMessage() {
    return super.getLocalizedMessage() + ": " + fails.stream().map(Throwable::getLocalizedMessage)
        .collect(Collectors.joining(";"));
  }

  @Override
  public String toString() {
    String message = getLocalizedMessage();
    var errorDescriptions = new StringBuilder();
    errorDescriptions.append(getClass().getName())
        .append(": ").append(message).append("\n");

    fails.forEach(e -> {
      var stream = new ByteArrayOutputStream();
      var printer = new PrintStream(stream);
      e.printStackTrace(printer);
      errorDescriptions.append(e.getMessage());
      errorDescriptions.append(stream);
    });

    return errorDescriptions.toString();
  }
}