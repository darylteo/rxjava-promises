package com.darylteo.vertx.promises.java.test;

import static org.vertx.testtools.VertxAssert.*;

import java.util.List;

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
    Promise<Long> promise = new Promise();
    vertx.setTimer(1000l, promise);

    promise.then(new PromiseFunction<Long, String>() {
      @Override
      public String call(Long timerID) {
        return "Hello World!";
      }
    }).then(new RepromiseFunction<String, String>() {
      @Override
      public Promise<String> call(final String t1) {
        final Promise<String> p = new Promise();
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
  
  
  
  @Test
  public void testAllHandler() {
    Promise<Long> promise1 = new Promise();
    Promise<Long> promise2 = new Promise();
    vertx.setTimer(1000l, promise1);
    vertx.setTimer(1010l, promise2);

    Promise.all(promise1, promise2).then(new PromiseFunction<List, String>() {
      @Override
      public String call(List results) {
        return "Hello World!";
      }
    }).then(new RepromiseFunction<String, String>() {
      @Override
      public Promise<String> call(final String t1) {
        final Promise<String> p = new Promise();
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
  
  @Test
  public void testAllRejectHandler() {
    Promise<Long> promise1 = new Promise();
    final Promise<Long> promise2 = new Promise();
    vertx.setTimer(1000l, promise1);
    vertx.setTimer(1010l, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        promise2.reject("cannot process"); 
      }
    });

    Promise.all(promise1, promise2).then(new PromiseFunction<List, String>() {
      @Override
      public String call(List results) {
        //should not be called
        return "Hello World!";
      }
    },
    new PromiseAction<Exception>() {
      @Override
      public void call(Exception e) {
        assertTrue(e != null);
        testComplete();
      }
    });
  }
  
}