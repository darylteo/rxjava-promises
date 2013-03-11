package com.darylteo.java.promises;

public interface FailureHandler<O> extends CompletedHandler<Exception> {
  public O handle(Exception e);
}
