package com.darylteo.rx.promises.java;

import com.darylteo.rx.promises.AbstractPromise;
import com.darylteo.rx.promises.java.functions.*;
import rx.functions.Function;
import rx.subjects.ReplaySubject;

/**
 * A Promise represents a request that will be fulfilled sometime in the future, most usually by an asynchrous task executed on the Vert.x Event Loop. It allows you to assign handlers to deal with the return results of asynchronus tasks, and to flatten "pyramids of doom" or "callback hell".
 * <p/>
 * <strong>Promise Rules</strong>
 * <ul>
 * <li>A Promise represents a value that is set in some future time (usually in another cycle of the event loop)</li>
 * <li>Each promise has three components: onFulfilled, onRejected, and onFinally</li>
 * <li>If the promise is fulfilled, onFulfilled is called with the value of the promise.</li>
 * <li>If the promise cannot be fulfilled for some reason, it is then rejected. onRejected is called with the reason for the rejection</li>
 * <li>A promise may be further deferred, at which point a new promise is provided. This can lead to a chain of promises.</li>
 * <li>If a promise if fulfilled, but onFulfilled is not provided, then the promise is fulfilled with the same value.</li>
 * <li>If a promise is rejected, but onRejected is not provided, then the next promise is rejected with the same reason</li>
 * <li>If onFinally is provided, it is resolved first before either fulfilling or rejecting the next promise (see previous two points)
 * <li>Either onFulfilled, or onRejected may return a new promise (i.e. a repromise) . When this happens, the subsequently created promise will be fulfilled with the value of the repromise when it is eventually fulfilled.</li>
 * </ul>
 * <p/>
 * <strong>Type-Safe Rules</strong>
 * <ul>
 * <li>All the type-safety rules are related to the output type of onFulfilled.</li>
 * <li>If onFulfilled returns type T, then onRejected must either return T, or null. This is facilitated through the use of PromiseFunction, and PromiseAction respectively.</li>
 * <li>As per the previous rule, if onFulfilled is an PromiseAction, then onRejected must also be an PromiseAction.</li>
 * <li>You may use a RepromiseFunction in place of a PromiseFunction that returns T.</li>
 * <li>If onFulfilled is not provided, it is assumed that it is defined in a future handler. As such, onRejected may not change the return type.</li>
 * <li>onFinally must be a Repromise or an Action. It does not accept any values. This is facilitated through the use of FinallyFunction and FinallyAction respectively.</li>
 * </ul>
 *
 * @param T - the data type of the result contained by this Promise.
 * @author Daryl Teo
 */
public class Promise<T> extends AbstractPromise<T> {
  public Promise() {
    super();
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
}
