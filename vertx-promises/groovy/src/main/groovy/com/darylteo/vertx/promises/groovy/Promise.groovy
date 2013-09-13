package com.darylteo.vertx.promises.groovy

import org.vertx.java.core.Handler

public class Promise<T> extends com.darylteo.rx.groovy.promises.Promise<T> implements Handler<T> {
  public static <T> Promise<T> defer() {
    return new Promise<T>();
  }

  @Override
  protected <O> Promise<O> _create() {
    return Promise.defer();
  }

  @Override
  public void handle(T event) {
    this.fulfill(event);
  }
}
