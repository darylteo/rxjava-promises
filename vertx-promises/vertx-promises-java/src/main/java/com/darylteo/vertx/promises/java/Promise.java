package com.darylteo.vertx.promises.java;

import java.util.List;

import org.vertx.java.core.Handler;

import rx.Observable;
import rx.functions.Function;

import com.darylteo.rx.promises.AbstractPromise;
import com.darylteo.vertx.promises.java.functions.FinallyAction;
import com.darylteo.vertx.promises.java.functions.FinallyFunction;
import com.darylteo.vertx.promises.java.functions.PromiseAction;
import com.darylteo.vertx.promises.java.functions.PromiseFunction;
import com.darylteo.vertx.promises.java.functions.RepromiseFunction;

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
  
  /**
   * Combines multiple promises into a single promise that is resolved when all of the input promises are resolved
   *
   * @param promises array of promises
   * @return Returns a single promise that will be resolved with a list of values,
   *         each value corresponding to the promise at the same index in the promises array.
   *         If any of the promises is resolved with a rejection, this resulting promise will
   *         be rejected with a list of values/exceptions.
   */
  @SuppressWarnings({"rawtypes" })
  public static Promise<List> all(final Promise... promises) {
    return (Promise<List>) AbstractPromise._all(Promise.class, promises);
  }
  
  /**
   * Combines multiple promises into a single promise that is resolved when all of the input promises are resolved,
   * before an timeout exception occurs. In case of timeout, result promise will be rejected with an TimeoutException
   *
   * @param timeout timeout in milliseconds
   * @param promises array of promises
   * @return Returns a single promise that will be resolved with a list of values,
   *         each value corresponding to the promise at the same index in the promises array.
   *         If any of the promises is resolved with a rejection, this resulting promise will
   *         be rejected with a list of values/exceptions.
   */
  @SuppressWarnings({"rawtypes" })
  public static Promise<List> all(long timeout, final Promise... promises) {
    return (Promise<List>) AbstractPromise._all(Promise.class, timeout, promises);
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
