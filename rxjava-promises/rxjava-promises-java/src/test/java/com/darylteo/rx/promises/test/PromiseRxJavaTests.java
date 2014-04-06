package com.darylteo.rx.promises.test;

import com.darylteo.rx.promises.java.Promise;
import com.darylteo.rx.promises.java.functions.PromiseAction;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class PromiseRxJavaTests {

  @Test
  public void testPromiseToRxSubscribe() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<String> result = new Result<String>();

    makePromise("Hello World")
      .toObservable()
      .subscribe(new Action1<String>() {
        @Override
        public void call(String value) {
          result.value = value;
          latch.countDown();
        }
      });

    latch.await();
    assertEquals("Hello World", result.value);
  }

  @Test
  public void testPromiseToRxMap() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<String> result = new Result<String>();

    makePromise("Hello World")
      .toObservable()
      .map(new Func1<String, String>() {
        @Override
        public String call(String value) {
          return value.toUpperCase();
        }
      })
      .subscribe(new Action1<String>() {
        @Override
        public void call(String value) {
          result.value = value;
          latch.countDown();
        }
      });

    latch.await();
    assertEquals("HELLO WORLD", result.value);
  }

  @Test
  public void testPromiseFromObservable() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<String> result = new Result<String>();

    Observable<String> obs = Observable.from(new String[]{
      "Hello",
      "World"
    });

    new Promise(obs)
      .then(new PromiseAction<String>() {
        @Override
        public void call(String message) {
          result.value = "World";
          latch.countDown();
        }
      });

    latch.await(2l, TimeUnit.SECONDS);
    assertEquals("World", result.value);
  }

  public Promise<String> makePromise(final String value) {
    final Promise<String> promise = new Promise();

    new Thread() {
      public void run() {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } finally {
          promise.fulfill(value);
        }
      }
    }.start();

    return promise;
  }

  private class Result<T> {
    T value;
  }
}
