package com.darylteo.rx.promises;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.AtomicObservableSubscription;
import rx.util.functions.Func1;
import rx.util.functions.Functions;

/**
 * A Promise represents a request that will be fulfilled sometime in the future,
 * most usually by an asynchrous task executed on the Vert.x Event Loop. It
 * allows you to assign handlers to deal with the return results of asynchronus
 * tasks, and to flatten "pyramids of doom" or "callback hell".
 * 
 * <strong>Promise Rules</strong>
 * <ul>
 * <li>A Promise represents a value that is set in some future time (usually in
 * another cycle of the event loop)</li>
 * <li>Each promise has three components: onFulfilled, onRejected, and onFinally
 * </li>
 * <li>If the promise is fulfilled, onFulfilled is called with the value of the
 * promise.</li>
 * <li>If the promise cannot be fulfilled for some reason, it is then rejected.
 * onRejected is called with the reason for the rejection</li>
 * <li>A promise may be further deferred, at which point a new promise is
 * provided. This can lead to a chain of promises.</li>
 * <li>If a promise if fulfilled, but onFulfilled is not provided, then the
 * promise is fulfilled with the same value.</li>
 * <li>If a promise is rejected, but onRejected is not provided, then the next
 * promise is rejected with the same reason</li>
 * <li>If onFinally is provided, it is resolved first before either fulfilling
 * or rejecting the next promise (see previous two points)
 * <li>Either onFulfilled, or onRejected may return a new promise (i.e. a
 * repromise) . When this happens, the subsequently created promise will be
 * fulfilled with the value of the repromise when it is eventually fulfilled.</li>
 * </ul>
 * 
 * <strong>Type-Safe Rules</strong>
 * <ul>
 * <li>All the type-safety rules are related to the output type of onFulfilled.</li>
 * <li>If onFulfilled returns type T, then onRejected must either return T, or
 * null. This is facilitated through the use of PromiseFunction, and
 * PromiseAction respectively.</li>
 * <li>As per the previous rule, if onFulfilled is an PromiseAction, then
 * onRejected must also be an PromiseAction.</li>
 * <li>You may use a RepromiseFunction in place of a PromiseFunction that
 * returns T.</li>
 * <li>If onFulfilled is not provided, it is assumed that it is defined in a
 * future handler. As such, onRejected may not change the return type.</li>
 * <li>onFinally must be a Repromise or an Action. It does not accept any
 * values. This is facilitated through the use of FinallyFunction and
 * FinallyAction respectively.</li>
 * </ul>
 * 
 * @author Daryl Teo
 * 
 * @param T
 *          - the data type of the result contained by this Promise.
 */
public class Promise<T> extends Observable<T> implements Observer<T> {
  private static enum STATE {
    PENDING,
    FULFILLED,
    REJECTED
  }

  private Promise<T> that = this;

  private T value = null;
  private Exception reason;

  private STATE state = STATE.PENDING;

  private Map<Subscription, Observer<T>> observers;

  public static <T> Promise<T> defer() {
    // LinkedHashMap used to preserve key insertion order
    final Map<Subscription, Observer<T>> observers = new LinkedHashMap<Subscription, Observer<T>>();

    final Promise<T> promise = new Promise<>(
        new Func1<Observer<T>, Subscription>() {
          @Override
          public Subscription call(Observer<T> observer) {
            final AtomicObservableSubscription subscription = new AtomicObservableSubscription();

            subscription.wrap(new Subscription() {
              @Override
              public void unsubscribe() {
                // on unsubscribe remove it from the map of outbound observers
                // to notify
                observers.remove(subscription);
              }
            });

            observers.put(subscription, observer);
            return subscription;
          }
        },
        observers
        );

    return promise;
  }

  protected Promise(Func1<Observer<T>, Subscription> onSubscribe,
      Map<Subscription, Observer<T>> observers) {
    super(onSubscribe);
    this.observers = observers;
  }

  /* ================== */
  /* Dynamic Defer Methods */
  /*
   * Deprecation warnings are for type-safety only, other languages may use this
   * freely without any issues
   */
  public <O> Promise<O> then(Object onFulfilled) {
    return this._then(onFulfilled, null, null);
  }

  public <O> Promise<O> then(Object onFulfilled, Object onRejected) {
    return this._then(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> fail(Object onRejected) {
    return this._then(null, onRejected, null);
  }

  public Promise<T> fin(Object onFinally) {
    return this._then(null, null, onFinally);
  }

  /* ================== */
  /* Strictly Typed Defer Methods */
  // then(onFulfilled)
  public <O> Promise<O> then(PromiseFunction<T, O> onFulfilled) {
    return this._then(onFulfilled, null, null);
  }

  public <O> Promise<O> then(RepromiseFunction<T, O> onFulfilled) {
    return this._then(onFulfilled, null, null);
  }

  public Promise<Void> then(PromiseAction<T> onFulfilled) {
    return this._then(onFulfilled, null, null);
  }

  // then(onFulfilled, onRejected)
  public <O> Promise<O> then(PromiseFunction<T, O> onFulfilled,
      PromiseFunction<Exception, O> onRejected) {
    return this._then(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> then(PromiseFunction<T, O> onFulfilled,
      RepromiseFunction<Exception, O> onRejected) {
    return this._then(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> then(PromiseFunction<T, O> onFulfilled,
      PromiseAction<Exception> onRejected) {
    return this._then(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> then(RepromiseFunction<T, O> onFulfilled,
      PromiseFunction<Exception, O> onRejected) {
    return this._then(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> then(RepromiseFunction<T, O> onFulfilled,
      RepromiseFunction<Exception, O> onRejected) {
    return this._then(onFulfilled, onRejected, null);
  }

  public <O> Promise<O> then(RepromiseFunction<T, O> onFulfilled,
      PromiseAction<Exception> onRejected) {
    return this._then(onFulfilled, onRejected, null);
  }

  public Promise<Void> then(PromiseAction<T> onFulfilled,
      PromiseAction<Exception> onRejected) {
    return this._then(onFulfilled, onRejected, null);
  }

  // fail(onRejected)
  public Promise<T> fail(PromiseFunction<Exception, T> onRejected) {
    return this._then(null, onRejected, null);
  }

  public Promise<T> fail(RepromiseFunction<Exception, T> onRejected) {
    return this._then(null, onRejected, null);
  }

  public Promise<T> fail(PromiseAction<Exception> onRejected) {
    return this._then(null, onRejected, null);
  }

  // fin(onFinally)
  public Promise<T> fin(FinallyFunction<?> onFinally) {
    return this._then(null, null, onFinally);
  }

  public Promise<T> fin(FinallyAction onFinally) {
    return this._then(null, null, onFinally);
  }

  /* ================== */
  /* Private Functions */
  private <O> Promise<O> _then(
      final Object onFulfilled,
      final Object onRejected,
      final Object onFinally)
  {
    // This is the next promise in the chain.
    // The handlers you see below will resolve their values and forward them to
    // this promise.
    final Promise<O> deferred = Promise.defer();

    // Create the Observer
    final Observer<T> observer = new Observer<T>() {
      @Override
      public void onCompleted() {
        this.evaluate();
      }

      @Override
      public void onError(Exception reason) {
        that.reason = reason;
        this.evaluate();
      }

      @Override
      public void onNext(T value) {
        that.value = value;
      }

      private void evaluate() {
        try {
          // onfinally and onFulfilled/onRejected are mutually exclusive
          if (onFinally != null) {
            Object result = Functions.from(onFinally).call();

            if (result != null) {
              // the finally block returned a promise, so we need to delay
              // fulfillment of the next promise until the returned promise is
              // fulfilled
              ((Promise<Void>) result).then(
                  new PromiseAction<Void>() {
                    @Override
                    public void call(Void v) {
                      deferred.fulfill((O) that.value);
                    }
                  }, new PromiseAction<Exception>() {
                    @Override
                    public void call(Exception e) {
                      deferred.reject(e);
                    }
                  });
            } else {
              // nothing was returned by the finally block. We can go ahead and
              // forward the value/reason held by this promise on to the next
              // one for resolution
              if (that.state == STATE.FULFILLED) {
                deferred.fulfill((O) that.value);
              } else {
                deferred.reject(that.reason);
              }
            }

            return;
          } // end Finally block

          // No finally block was provided, thus we need to evaluate fulfillment
          // or rejection. If the appropriate handler is not provided, it is
          // forwarded to the next promise
          if (that.state == STATE.FULFILLED) {
            if (onFulfilled != null) {
              Object result = Functions.from(onFulfilled).call(that.value);
              evalResult(result);
            } else {
              // Sends the value forward. We assume that the casting will pass
              deferred.fulfill((O) that.value);
            }
            return;
          }

          if (that.state == STATE.REJECTED) {
            if (onRejected != null) {
              // Allow this handler to recover from the rejection
              Object result = Functions.from(onRejected).call(that.reason);
              evalResult(result);
            } else {
              // Forward it to the next promise
              deferred.reject(that.reason);
            }
            return;
          }
        } catch (Exception e) {
          // On any exception in the handlers above, we should throw the
          // exception to the next promise

          deferred.reject(e);
        }
      }

      // takes a result and either converts it to a promise or sends it forward
      // for fulfillment
      @SuppressWarnings("unchecked")
      private void evalResult(Object result) {
        if (result instanceof Promise) {
          deferred.become((Promise<O>) result);
        } else {
          deferred.fulfill((O) result);
        }
      }
    };

    this.subscribe(observer);

    // Immediately notify observer if result of this promise has already been
    // determined
    if (this.state == STATE.FULFILLED) {
      observer.onNext(this.value);
      observer.onCompleted();
    } else if (this.state == STATE.REJECTED) {
      observer.onError(this.reason);
    }

    return deferred;
  }

  /* Result Methods */
  public void fulfill(T value) {
    if (this.state != STATE.PENDING) {
      throw new IllegalStateException();
    }

    this.state = STATE.FULFILLED;
    this.value = value;

    // A copy of the observers is taken first, in case more observers are added
    // after.
    List<Observer<T>> observerList = new ArrayList<>(this.observers.values());
    for (Observer<T> obs : observerList) {
      obs.onNext(this.value);
      obs.onCompleted();
    }
  }

  public void reject(Exception reason) {
    if (this.state != STATE.PENDING) {
      throw new IllegalStateException();
    }

    this.state = STATE.REJECTED;
    this.reason = reason;

    List<Observer<T>> observerList = new ArrayList<>(this.observers.values());
    for (Observer<T> obs : observerList) {
      obs.onError(this.reason);
    }
  }

  public void reject(Object reason) {
    this.reject(new Exception(reason.toString()));
  }

  public void become(Promise<T> other) {
    other.subscribe(this);
  }

  @Override
  public void onCompleted() {
    // no op
  }

  @Override
  public void onError(Exception e) {
    this.reject(e);
  }

  @Override
  public void onNext(T value) {
    // Grab only the first value
    // Ignore others if they come in
    if (this.state == STATE.PENDING) {
      this.fulfill(value);
    }
  }
}
