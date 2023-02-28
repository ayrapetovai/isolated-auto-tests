package com.example.testing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AggregateException extends RuntimeException {

  private final List<? extends Exception> fails;

  public AggregateException(List<? extends Exception> fails) {
    this.fails = new ArrayList<>(fails);
  }

  @Override
  public String getMessage() {
    return fails.stream().map(Exception::getMessage)
        .collect(Collectors.joining(";"));
  }

  @Override
  public String getLocalizedMessage() {
    return fails.stream().map(Exception::getLocalizedMessage)
        .collect(Collectors.joining(";"));
  }

  @Override
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