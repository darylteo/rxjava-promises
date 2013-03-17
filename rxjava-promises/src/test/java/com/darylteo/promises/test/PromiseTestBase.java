package com.darylteo.promises.test;

import static org.vertx.testtools.VertxAssert.testComplete;

import org.vertx.java.core.Handler;
import org.vertx.java.core.SimpleHandler;
import org.vertx.testtools.TestVerticle;

import com.darylteo.promises.Promise;

public abstract class PromiseTestBase extends TestVerticle {
  protected Promise<String> makePromise(final String message) {
    final Promise<String> promise = Promise.defer();

    vertx.runOnLoop(new SimpleHandler() {
      @Override
      public void handle() {
        System.out.print("Working.");
        for (int i = 0; i < 10; i++) {
          System.out.print(".");

          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        System.out.println();
        promise.fulfill(message);
      }
    });

    return promise;
  }

  protected void endLater() {
    vertx.setTimer(2l, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        testComplete();
      }
    });
  }
}
