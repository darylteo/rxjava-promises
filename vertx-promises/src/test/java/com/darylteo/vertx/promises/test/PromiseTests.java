package com.darylteo.vertx.promises.test;

import org.junit.Test;
import org.vertx.testtools.TestVerticle;

import com.darylteo.rx.promises.functions.PromiseAction;
import com.darylteo.vertx.promises.Promise;

public class PromiseTests extends TestVerticle {
  @Test
  public void testHandler() {
    Promise<Long> promise = Promise.defer();
    vertx.setTimer(2000l, promise);

    promise.then(new PromiseAction<Long>() {
      @Override
      public void call(Long timerID) {
        org.vertx.testtools.VertxAssert.testComplete();
      }
    });
  }
}