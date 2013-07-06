package com.darylteo.rx.promises;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.darylteo.rx.promises.functions.PromiseAction;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.operators.AtomicObservableSubscription;
import rx.util.functions.Func1;
import rx.util.functions.Functions;

public abstract class AbstractPromise<T> extends Observable<T> implements Observer<T> {
  public static enum STATE {
    PENDING,
    FULFILLED,
    REJECTED
  }

  /* Properties */
  private AbstractPromise<T> that = this;

  private Map<Subscription, Observer<T>> observers;

  public Collection<Observer<T>> getObservers() {
    return this.observers.values();
  }

  private STATE state = STATE.PENDING;

  public STATE getState() {
    return this.state;
  }

  private T value = null;

  public T getValue() {
    return this.value;
  }

  private Exception reason;

  public Exception getReason() {
    return this.reason;
  }

  /* Constructor */
  public AbstractPromise(final Map<Subscription, Observer<T>> observers) {
    super(new Func1<Observer<T>, Subscription>() {
      @Override
      public Subscription call(Observer<T> observer) {
        final rx.operators.AtomicObservableSubscription subscription = new AtomicObservableSubscription();

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
    });

    this.observers = observers;
  }

  /* ================== */
  /* Main Defer Function */
  protected <O> AbstractPromise<O> _then(
    final Object onFulfilled,
    final Object onRejected,
    final Object onFinally)
  {
    // This is the next promise in the chain.
    // The handlers you see below will resolve their values and forward them
    // to this promise.
    final AbstractPromise<O> deferred = this._create();

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
              ((AbstractPromise<Void>) result).then(
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
        if (result instanceof AbstractPromise) {
          deferred.become((AbstractPromise<O>) result);
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
    List<Observer<T>> observerList = new ArrayList<>(this.getObservers());
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

    List<Observer<T>> observerList = new ArrayList<>(this.getObservers());
    for (Observer<T> obs : observerList) {
      obs.onError(this.reason);
    }
  }

  public void reject(Object reason) {
    this.reject(new Exception(reason.toString()));
  }

  public void become(AbstractPromise<T> other) {
    other.subscribe(this);
  }

  /* Observable Methods */
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

  /* Abstract Methods */
  /* ================== */
  /* Dynamic Defer Methods */
  /*
   * Deprecation warnings are for type-safety only, other languages may use this
   * freely without any issues
   * 
   * When overriding, make sure you narrow the return type to your specific
   * implementation
   */
  public abstract <O> AbstractPromise<O> then(Object onFulfilled);

  public abstract <O> AbstractPromise<O> then(Object onFulfilled, Object onRejected);

  public abstract <O> AbstractPromise<O> fail(Object onRejected);

  public abstract AbstractPromise<T> fin(Object onFinally);

  /* Subclasses should return an instance of their respective implementation */
  protected abstract <O> AbstractPromise<O> _create();
}
