package com.darylteo.rx.promises.groovy

import com.darylteo.rx.promises.AbstractPromise
import com.darylteo.rx.promises.functions.Action0
import com.darylteo.rx.promises.functions.Func1

public class Promise<T> extends AbstractPromise<T> {
  public Promise() {
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

  private <O> Promise<O> promise(Closure<O> onFulfilled, Closure<O> onRejected, Closure<O> onFinally) {
    return (Promise<O>) super._then(onFulfilled as Func1<T, O>, onRejected as Func1<T, O>, onFinally as Action0)
  }
}