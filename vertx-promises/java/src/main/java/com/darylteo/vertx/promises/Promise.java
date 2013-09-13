package com.darylteo.vertx.promises;

import org.vertx.java.core.Handler;

public class Promise<T> extends com.darylteo.rx.java.promises.Promise<T> implements Handler<T> {
  public static <T> Promise<T> defer() {
    return new Promise<T>();
  }

  @Override
  public void handle(T event) {
    this.fulfill(event);
  }
}
