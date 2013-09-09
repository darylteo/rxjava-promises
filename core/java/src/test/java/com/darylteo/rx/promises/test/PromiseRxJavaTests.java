package com.darylteo.rx.promises.test;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import rx.util.functions.Action1;
import rx.util.functions.Func1;

import com.darylteo.rx.promises.Promise;

public class PromiseRxJavaTests {

  @Test
  public void testPromiseToRxSubscribe() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<String> result = new Result<>();

    makePromise("Hello World")
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
    final Result<String> result = new Result<>();

    makePromise("Hello World")
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

  public Promise<String> makePromise(final String value) {
    final Promise<String> promise = Promise.defer();

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
