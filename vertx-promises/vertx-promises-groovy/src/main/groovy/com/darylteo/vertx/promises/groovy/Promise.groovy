package com.darylteo.vertx.promises.groovy

import java.util.List;

import com.darylteo.rx.promises.AbstractPromise

import org.vertx.java.core.Handler

import rx.functions.Action0
import rx.functions.Func1

public class Promise<T> extends AbstractPromise<T> implements Handler<T> {
  public Promise() {
    super();
  }

  public Promise(rx.Observable<T> source) {
    super(source);
  }


  public <O> Promise<O> then(Map m = [:]) {
    return this.promise(m.onFulfilled, m.onRejected, null)
  }

  public <O> Promise<O> then(Closure<O> onFulfilled, Closure<O> onRejected = null) {
    return this.promise(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> fail(Closure<O> onRejected) {
    return this.promise(null, onRejected, null);
  }

  public <O> Promise<O> fin(Closure<O> onFinally) {
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
  public static Promise<List> all(final Promise... promises) {
    return AbstractPromise._all(Promise.class, promises);
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
  public static Promise<List> all(long timeout, final Promise... promises) {
    return AbstractPromise._all(Promise.class, timeout, promises);
  }

  private <O> Promise<O> promise(Closure<O> onFulfilled, Closure<O> onRejected, Closure<O> onFinally) {
    return (Promise<O>) super._then(onFulfilled as Func1<T, O>, onRejected as Func1<T, O>, onFinally as Action0<?>)
  }

  @Override
  public void handle(T event) {
    this.fulfill(event);
  }
}
