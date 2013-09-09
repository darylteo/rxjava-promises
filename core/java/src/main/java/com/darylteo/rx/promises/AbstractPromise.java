package com.darylteo.rx.promises;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.operators.SafeObservableSubscription;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Func0;
import rx.util.functions.Func1;
import rx.util.functions.Function;
import rx.util.functions.Functions;

import com.darylteo.rx.promises.functions.FinallyAction;
import com.darylteo.rx.promises.functions.FinallyFunction;
import com.darylteo.rx.promises.functions.PromiseAction;

public abstract class AbstractPromise<T> extends Observable<T> implements Observer<T> {
  public static enum STATE {
    PENDING,
    FULFILLED,
    REJECTED
  }

  /* Properties */
  private AbstractPromise<T> that = this;

  private LinkedHashMap<Subscription, Observer<? super T>> observers;
  private STATE state = STATE.PENDING;
  private T value = null;
  private Throwable reason;

  public STATE getState() {
    return this.state;
  }

  public T getValue() {
    return this.value;
  }

  public Throwable getReason() {
    return this.reason;
  }

  /* Constructor */
  public AbstractPromise(final LinkedHashMap<Subscription, Observer<? super T>> observers) {
    super(new Observable.OnSubscribeFunc<T>() {
      @Override
      public Subscription onSubscribe(Observer<? super T> observer) {
        final rx.operators.SafeObservableSubscription subscription = new
          SafeObservableSubscription();

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
    final Function onFulfilled,
    final Function onRejected,
    final Function onFinally)
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
      public void onError(Throwable reason) {
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
            evaluateFinally();
            return;
          }

          // No finally block was provided, thus we need to evaluate fulfillment or rejection.
          // If the appropriate handler is not provided, it is forwarded to the next promise
          if (that.state == STATE.FULFILLED) {
            evaluateFulfilled();
            return;
          }

          if (that.state == STATE.REJECTED) {
            evaluateRejected();
            return;
          }
        } catch (Throwable e) {
          // On any exception in the handlers above, we should throw the
          // exception to the next promise

          deferred.reject(e);
        }
      }

      private void evaluateFinally() {
        AbstractPromise<?> result = callFinally();

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

      }

      private void evaluateFulfilled() {
        if (onFulfilled != null) {
          Object result = callFunction(onFinally, that.value);
          evalResult(result);
        } else {
          // Sends the value forward. We assume that the casting will pass
          deferred.fulfill((O) that.value);
        }
      }

      private void evaluateRejected() {
        if (onRejected != null) {
          // Allow this handler to recover from the rejection
          Object result = callFunction(onRejected, that.reason);
          evalResult(result);
        } else {
          // Forward it to the next promise
          deferred.reject(that.reason);
        }
      }

      private AbstractPromise<?> callFinally() {
        if (onFinally instanceof FinallyFunction) {
          return (AbstractPromise<?>) ((FinallyFunction) onFinally).call();
        }

        ((FinallyAction) onFinally).call();
        return null;
      }

      private <O> Object callFunction(Function function, O value) throws IllegalArgumentException {
        if (function instanceof Action0) {
          ((Action0) function).call();
          return null;
        }

        if (function instanceof Action1<?>) {
          ((Action1<O>) function).call(value);
          return null;
        }

        if (function instanceof Func0<?>) {
          return ((Func0<?>) function).call();
        }

        if (function instanceof Func1<?, ?>) {
          return ((Func1<O, ? super Object>) function).call(value);
        }

        throw new IllegalArgumentException("Could not correctly invoke callback function with type " + function.getClass().toString());
      }

      // takes a result and either converts it to a promise or sends it forward for fulfillment
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

    // A copy of the observers is taken first, in case more observers are added after.
    List<Observer<? super T>> observerList = new ArrayList<>(this.observers.values());
    for (Observer<? super T> obs : observerList) {
      obs.onNext(this.value);
      obs.onCompleted();
    }
  }

  public void reject(Throwable reason) {
    if (this.state != STATE.PENDING) {
      throw new IllegalStateException();
    }

    this.state = STATE.REJECTED;
    this.reason = reason;

    // A copy of the observers is taken first, in case more observers are added after.
    List<Observer<? super T>> observerList = new ArrayList<>(this.observers.values());
    for (Observer<? super T> obs : observerList) {
      obs.onError(this.reason);
    }
  }

  public void reject(Object reason) {
    this.reject(new Exception(reason.toString()));
  }

  public void become(AbstractPromise<T> other) {
    other.subscribe(this);
  }

  protected <O> AbstractPromise<O> then(
    final Function onFulfilled
    ) {
    return this._then(onFulfilled, null, null);
  }

  protected <O> AbstractPromise<O> then(
    final Function onFulfilled,
    final Function onRejected
    ) {
    return this._then(onFulfilled, onRejected, null);
  }

  protected <O> AbstractPromise<O> fin(
    final Function onFinally
    ) {
    return this._then(null, null, onFinally);
  }

  /* Observable Methods */
  @Override
  public void onCompleted() {
    // no op
  }

  @Override
  public void onError(Throwable e) {
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

  /* Private Methods */
  @SuppressWarnings("unchecked")
  private <O> AbstractPromise<O> _create() {
    try {
      return (AbstractPromise<O>) this.getClass().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

}
