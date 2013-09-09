package com.darylteo.rx.groovy.promises

import rx.Observer
import rx.Subscription
import rx.util.functions.Action0
import rx.util.functions.Func1

import com.darylteo.rx.promises.AbstractPromise

public class Promise<T> extends AbstractPromise<T> {
  public static <T> Promise<T> defer() {
    return new Promise<T>();
  }

  public Promise() {
    super(new LinkedHashMap<Subscription, Observer<? super T>>());
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
    return (Promise<O>) super._then(onFulfilled as Func1<T,O>, onRejected as Func1<T,O>, onFinally as Action0<?>)
  }
}
