package com.darylteo.rx.promises.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.darylteo.rx.promises.java.Promise;
import com.darylteo.rx.promises.java.functions.PromiseAction;

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
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void testPromiseAll() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);

    Promise p1 = new Promise();
    Promise p2 = new Promise();

    final Result<Integer> result = new Result<Integer>();

    Promise.all(p1, p2).then(new PromiseAction<List>() {
      public void call(List l) {
        result.value = 0;
        for (Object object : l) {
          result.value += (Integer) object;
        }
      }
    });

    p1.fulfill(2);
    p2.fulfill(3);

    latch.await(2, TimeUnit.SECONDS);
    assertEquals(result.value, (Integer) 5);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void testPromiseAllRejectOnTimeout() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);

    Promise p1 = new Promise();
    Promise p2 = new Promise();

    final Result result = new Result();

    Promise.all(100, p1, p2).then(new PromiseAction<List>() {
      public void call(List l) {
        //should't be called
        result.value = l;
      }
    },new PromiseAction<Exception>() {
      public void call(Exception e) {
        result.value = e;
      }
    });

    p1.fulfill(2);
    latch.await(1, TimeUnit.SECONDS);
    p2.fulfill(3);

    assertTrue(result.value instanceof TimeoutException);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void testPromiseAllPassOnTimeout() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);

    Promise p1 = new Promise();
    Promise p2 = new Promise();

    final Result result = new Result();

    Promise.all(100, p1, p2).then(new PromiseAction<List>() {
      public void call(List l) {
        //should't be called
        result.value = l;
      }
    },new PromiseAction<Exception>() {
      public void call(Exception e) {
        result.value = e;
      }
    });

    p1.fulfill(2);
    p2.fulfill(3);
    latch.await(1, TimeUnit.SECONDS);

    assertTrue(result.value instanceof List);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void testPromiseAllReject() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);

    Promise p1 = new Promise();
    Promise p2 = new Promise();

    final Result<Exception> result = new Result<Exception>();

    Promise.all(p1, p2).then(new PromiseAction<List>() {
      public void call(List l) {
        // not used for this test
      }
    }, new PromiseAction<Exception>() {
      public void call(Exception e) {
        result.value = e;
      }
    });

    p1.fulfill(2);
    String reason = "Reject Reason";
    p2.reject(reason);

    latch.await(2, TimeUnit.SECONDS);
    assertTrue(result.value instanceof Exception);
    assertTrue(((Exception) result.value).getMessage().contains(reason));
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
