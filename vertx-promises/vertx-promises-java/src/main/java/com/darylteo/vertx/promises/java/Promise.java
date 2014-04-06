package com.darylteo.vertx.promises.java;

import com.darylteo.rx.promises.AbstractPromise;
import com.darylteo.vertx.promises.java.functions.*;
import org.vertx.java.core.Handler;
import rx.Observable;
import rx.functions.Function;

public class Promise<T> extends AbstractPromise<T> implements Handler<T> {
  public Promise() {
    super();
  }

  public Promise(Observable<T> source) {
    super(source);
  }

  /* ================== */
  /* Strictly Typed Defer Methods */
  // then(onFulfilled)
  public <O> Promise<O> then(PromiseFunction<T, O> onFulfilled) {
    return this.promise(onFulfilled, null, null);
  }

  public <O> Promise<O> then(RepromiseFunction<T, O> onFulfilled) {
    return this.promise(onFulfilled, null, null);
  }

  public Promise<Void> then(PromiseAction<T> onFulfilled) {
    return this.promise(onFulfilled, null, null);
  }

  // then(onFulfilled, onRejected)
  public <O> Promise<O> then(PromiseFunction<T, O> onFulfilled,
                             PromiseFunction<Exception, O> onRejected) {
    return this.promise(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> then(PromiseFunction<T, O> onFulfilled,
                             RepromiseFunction<Exception, O> onRejected) {
    return this.promise(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> then(PromiseFunction<T, O> onFulfilled,
                             PromiseAction<Exception> onRejected) {
    return this.promise(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> then(RepromiseFunction<T, O> onFulfilled,
                             PromiseFunction<Exception, O> onRejected) {
    return this.promise(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> then(RepromiseFunction<T, O> onFulfilled,
                             RepromiseFunction<Exception, O> onRejected) {
    return this.promise(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> then(RepromiseFunction<T, O> onFulfilled,
                             PromiseAction<Exception> onRejected) {
    return this.promise(onFulfilled, onRejected, null);
  }

  public Promise<Void> then(PromiseAction<T> onFulfilled,
                            PromiseAction<Exception> onRejected) {
    return this.promise(onFulfilled, onRejected, null);
  }

  // fail(onRejected)
  public Promise<T> fail(PromiseFunction<Exception, T> onRejected) {
    return this.promise(null, onRejected, null);
  }

  public Promise<T> fail(RepromiseFunction<Exception, T> onRejected) {
    return this.promise(null, onRejected, null);
  }

  public Promise<T> fail(PromiseAction<Exception> onRejected) {
    return this.promise(null, onRejected, null);
  }

  // fin(onFinally)
  public Promise<T> fin(FinallyFunction<?> onFinally) {
    return this.promise(null, null, onFinally);
  }

  public Promise<T> fin(FinallyAction onFinally) {
    return this.promise(null, null, onFinally);
  }

  @SuppressWarnings("unchecked")
  protected <O> Promise<O> promise(Function onFulfilled, Function onRejected, Function onFinally) {
    return (Promise<O>) super._then(onFulfilled, onRejected, onFinally);
  }

  @Override
  public void handle(T event) {
    this.fulfill(event);
  }
}
