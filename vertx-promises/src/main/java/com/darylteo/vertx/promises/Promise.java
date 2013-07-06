package com.darylteo.vertx.promises;

import org.vertx.java.core.Handler;

public final class Promise<T> extends com.darylteo.rx.promises.Promise<T> implements Handler<T> {
  public static <T> Promise<T> defer() {
    return new Promise<T>();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <O> Promise<O> _then(Object onFulfilled, Object onRejected, Object onFinally) {
    return (Promise<O>) super._then(onFulfilled, onRejected, onFinally);
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
