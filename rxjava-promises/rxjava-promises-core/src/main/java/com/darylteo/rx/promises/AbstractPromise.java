package com.darylteo.rx.promises;

import rx.Observable;
import rx.Observer;
import rx.exceptions.OnErrorThrowable;
import rx.functions.*;
import rx.subjects.ReplaySubject;

public abstract class AbstractPromise<T> implements Observer<T> {
  public static enum STATE {
    PENDING,
    FULFILLED,
    REJECTED
  }

  /* Properties */
  private AbstractPromise<T> that = this;

  private ReplaySubject<T> subject;
  private Observable<T> obs;

  private STATE state = STATE.PENDING;
  private T value = null;
  private Throwable reason;

  public STATE getState() {
    return this.state;
  }

  public boolean isPending() {
    return this.state == STATE.PENDING;
  }

  public boolean isFulfilled() {
    return this.state == STATE.FULFILLED;
  }

  public boolean isRejected() {
    return this.state == STATE.REJECTED;
  }

  public T getValue() {
    return this.value;
  }

  public Throwable getReason() {
    return this.reason;
  }

  /* Constructor */
  public AbstractPromise() {
    this(null);
  }

  public AbstractPromise(Observable<T> source) {
    this.subject = ReplaySubject.create();
    this.obs = this.subject.last();

    if (source != null) {
      source.subscribe(this.subject);
    }

    // promise states
    this.obs.subscribe(new Observer<T>() {
      @Override
      public void onCompleted() {
        that.state = STATE.FULFILLED;
      }

      @Override
      public void onError(Throwable reason) {
        that.state = STATE.REJECTED;
        that.reason = reason;
      }

      @Override
      public void onNext(T value) {
        that.value = value;
      }
    });
  }

  /* ================== */
  /* Main Defer Function */
  protected <O> AbstractPromise<O> _then(
    final Function onFulfilled,
    final Function onRejected,
    final Function onFinally) {
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
        this.evaluate();
      }

      @Override
      public void onNext(T value) {

      }

      private void evaluate() {
        try {
          // onfinally and onFulfilled/onRejected are mutually exclusive
          // note: this implementation of finally is closer to "onComplete" rather than "finallyDo"
          // in that it fires immediately when the previous Promise is fulfilled, rather than
          // for the entire sequence of Observables to complete its sequence.
          if (onFinally != null) {
            evaluateFinally();
            return;
          }

          // No finally block was provided, thus we need to evaluate fulfillment
          // or rejection.
          // If the appropriate handler is not provided, it is forwarded to the
          // next promise
          if (that.state == STATE.FULFILLED) {
            evaluateFulfilled();
            return;
          }

          if (that.state == STATE.REJECTED) {
            evaluateRejected();
            return;
          }
        } catch (Throwable e) {
          // if a throwable is given (instead of purely an Exception),
          // we want to wrap it in a Exception

          deferred.reject(e);
        }
      }

      private void evaluateFinally() {
        AbstractPromise<?> result = callFinally();

        if (result != null) {
          // the finally block returned a promise, so we need to delay
          // fulfillment of the next promise until the returned promise is
          // fulfilled
          ((AbstractPromise<? super Object>) result)._then(
            new Action1<Object>() {
              @Override
              public void call(Object v) {
                deferred.fulfill((O) that.value);
              }
            }, new Action1<Throwable>() {
              @Override
              public void call(Throwable e) {
                deferred.reject(e);
              }
            }, null
          );
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
          Object result = callFunction(onFulfilled, that.value);
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
        if (onFinally instanceof Func0) {
          return (AbstractPromise<?>) ((Func0<?>) onFinally).call();
        }

        ((Action0) onFinally).call();
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

    this.obs.subscribe(observer);

    return deferred;
  }

  /* Result Methods */
  public void fulfill(T value) {
    this.subject.onNext(value);
    this.subject.onCompleted();
  }

  public void reject(Object reason) {
    this.subject.onError(new Exception(reason.toString()));
  }

  public void reject(Exception reason) {
    this.subject.onError(reason);
  }

  public void reject(Throwable reason) {
    if (reason instanceof Exception) {
      this.subject.onError(reason);
    } else {
      this.subject.onError(OnErrorThrowable.from(reason));
    }
  }

  public void become(AbstractPromise<T> other) {
    other.subject.subscribe(this);
  }

  /* Observable Methods */
  @Override
  public void onCompleted() {
    this.fulfill(this.value);
  }

  @Override
  public void onError(Throwable e) {
    this.reject(e);
  }

  @Override
  public void onNext(T value) {
    this.value = value;
  }

  /* rx adapter */
  public Observable<T> toObservable() {
    return this.obs;
  }

  /* Private Methods */
  @SuppressWarnings("unchecked")
  private <O> AbstractPromise<O> _create() {
    try {
      return (AbstractPromise<O>) this.getClass().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
