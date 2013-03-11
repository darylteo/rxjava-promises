package com.darylteo.promises.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import com.darylteo.promises.FailureFunction;
import com.darylteo.promises.java.PromiseHandler;

public class Promise {
  private com.darylteo.promises.Promise<Object> _promise;

  public Promise() {
    this(com.darylteo.promises.Promise.defer());
  }

  public Promise(com.darylteo.promises.Promise<Object> promise) {
    this._promise = promise;
  }

//  public Promise then(final Function fulfilled) {
//    return this.then(fulfilled, null);
//  }
//
//  public Promise then(final Function fulfilled, final Function rejected) {
//    return new Promise(_promise.then(promiseHandler(fulfilled), failureHandler(rejected)));
//  }
//
//  public Promise fail(final Function rejected) {
//    return new Promise(_promise.fail(failureHandler(rejected)));
//  }
//
//  public Promise fin(final Function finaled) {
//    return new Promise(_promise.fin(finalHandler(finaled)));
//  }

  public void fulfill(Object value) {
    _promise.fulfill(value);
  }

  public void reject(Object e) {
    _promise.reject(new Exception(e.toString()));
  }

  private Object invoke(Function function, Object... args) {
    if (function == null) {
      return null;
    }

    Context context = Context.enter();

    try {
      Scriptable scope = function.getParentScope();
      Scriptable that = context.newObject(scope);
      Object result = function.call(
          context, scope, that, args);

      return result;
    } finally {
      Context.exit();
    }
  }

  private PromiseHandler<Object, Object> promiseHandler(final Function function) {
    return new PromiseHandler<Object, Object>() {
      @Override
      public Object handle(Object value) throws Exception {
        return invoke(function, value);
      }
    };
  }

  private PromiseHandler<Void, Object> finalHandler(final Function function) {
    return new PromiseHandler<Void, Object>() {
      @Override
      public Object handle(Void nothing) throws Exception {
        return invoke(function);
      }
    };
  }

  private FailureFunction<Object> failureHandler(final Function function) {
    return new FailureFunction<Object>() {
      @Override
      public Object handle(Exception e) {
        return invoke(function, e);
      }
    };
  }
}
