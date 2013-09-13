package com.darylteo.vertx.promises.java.test;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import com.darylteo.vertx.promises.java.Promise;
import com.darylteo.vertx.promises.java.functions.PromiseAction;
import com.darylteo.vertx.promises.java.functions.PromiseFunction;
import com.darylteo.vertx.promises.java.functions.RepromiseFunction;

public class PromiseTests extends TestVerticle {
  @Test
  public void testHandler() {
    Promise<Long> promise = Promise.defer();
    vertx.setTimer(1000l, promise);

    promise.then(new PromiseFunction<Long, String>() {
      @Override
      public String call(Long timerID) {
        return "Hello World!";
      }
    }).then(new RepromiseFunction<String, String>() {
      @Override
      public Promise<String> call(final String t1) {
        final Promise<String> p = Promise.defer();
        vertx.setTimer(1000l, new Handler<Long>() {
          @Override
          public void handle(Long event) {
            p.fulfill(t1.toUpperCase());
          }
        });

        return p;
      }
    }).then(new PromiseAction<String>() {
      @Override
      public void call(String t1) {
        assertEquals(t1, "HELLO WORLD!");
        testComplete();
      }

    });
  }
}