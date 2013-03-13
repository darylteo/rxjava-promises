package com.darylteo.promises.test;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.junit.Test;

import rx.Observable;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

import com.darylteo.promises.Promise;
import com.darylteo.promises.PromiseAction;

public class PromiseRxJavaTests extends PromiseTestBase {

  @Test
  public void testPromiseToRxSubscribe() {
    makePromise("Hello World")
        .subscribe(new Action1<String>() {
          @Override
          public void call(String value) {
            assertEquals(value, "Hello World");
            testComplete();
          }
        });

  }

  @Test
  public void testPromiseToRxMap() {
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
            assertEquals(value, "HELLO WORLD");
            testComplete();
          }
        });
  }

  @Test
  public void testRxToPromise() {
    Observable<String> obs = Observable.from("Hello", "World");

    Promise<String> p = Promise.defer();

    p.then(new PromiseAction<String>() {
      @Override
      public void call(String value) {
        assertEquals(value, "Hello");
        testComplete();
      }
    });

    obs.subscribe(p);
  }
}
