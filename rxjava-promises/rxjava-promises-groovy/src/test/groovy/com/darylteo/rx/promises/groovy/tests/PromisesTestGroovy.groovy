package com.darylteo.rx.promises.groovy.tests

import static org.junit.Assert.*

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.junit.Test

import com.darylteo.rx.promises.groovy.Promise

/* http://promises-aplus.github.io/promises-spec/ */

class PromisesTestGroovy {
  @Test
  public void testStates() {
    def promise = new Promise()
    assertTrue(promise.pending)
    promise.fulfill('Hello')
    assertTrue(promise.fulfilled)
    assertEquals('Hello', promise.value)
    promise.reject(false)
    assertTrue(promise.fulfilled)
    assertNull(promise.reason)
    assertEquals('Hello', promise.value)
    promise.fulfill('World')
    assertTrue(promise.fulfilled)
    assertEquals('Hello', promise.value)

    promise = new Promise()
    assertTrue(promise.pending)
    promise.reject('Foo')
    assertTrue(promise.rejected)
    assertEquals('Foo', promise.reason?.message)
    promise.fulfill(true)
    assertTrue(promise.rejected)
    assertNull(promise.value)
    assertEquals('Foo', promise.reason?.message)
    promise.reject('Bar')
    assertTrue(promise.rejected)
    assertEquals('Foo', promise.reason?.message)
  }

  @Test
  public void testThenSyntax() {
    /* Both onFulfilled and onRejected are optional arguments */
    makePromise('Hello').then {
    }
    makePromise('Hello').then(null, {
    })
    makePromise('Hello').then({
    }, {
    })
    makePromise('Hello').then onFulfilled: {
    }
    makePromise('Hello').then onRejected: {
    }
    makePromise('Hello').then onFulfilled: {
    }, onRejected: {
    }
    makePromise('Hello').then()
  }

  @Test
  public void testThen() {
    CountDownLatch latch = new CountDownLatch(3)

    makePromise('Hello').then { result ->
      assertEquals 'Hello', result
      latch.countDown()
      return result.toUpperCase()
    }.then {
      assertEquals 'HELLO', it
      latch.countDown()
    }.then { makePromise('Foo') }.then { result ->
      assertEquals 'Foo', result
      latch.countDown()
    }

    latch.await(2l, TimeUnit.SECONDS);
    assertEquals 0, latch.count
  }

  @Test
  public void testReject() {
    CountDownLatch latch = new CountDownLatch(2)
    def invalids = []

    makeRejection('Hello').then { result -> invalids += result }
    makeRejection('Hello').then({ result -> invalids += result }, { latch.countDown() })
    makeRejection('Hello').then onFulfilled: { result -> invalids += result }, onRejected: { latch.countDown() }

    latch.await(2l, TimeUnit.SECONDS);
    assertTrue invalids.empty
    assertEquals 0, latch.count
  }

  @Test
  public void testFinally() {
    CountDownLatch latch = new CountDownLatch(5)
    def invalids = []

    makeRejection('Hello').fin { latch.countDown() }
    makeRejection('Hello').fin {
      latch.countDown()
      return 'Foo'
    }.then({ result -> invalids += result }, { latch.countDown() })
    makeRejection('Hello').fin {
      latch.countDown()
      makePromise('Foo')
    }.then({ result -> invalids += result }, { latch.countDown() })
    makeRejection('Hello').then { result -> invalids += result }.fin { latch.countDown() }.fail { latch.countDown() }

    latch.await(2l, TimeUnit.SECONDS);
    assertTrue invalids.empty
    assertEquals 0, latch.count
  }
  
  @Test
  public void testPromiseAll() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);

    Promise p1 = new Promise();
    Promise p2 = new Promise();

    def result;
    println "start : " + result

    Promise.all(p1, p2).then { l ->
        result = 0;
        println "all done : " + result
        l.each {
          result += (Integer) it;
          println "result : " + result
        }
      };

    p1.fulfill(2);
    p2.fulfill(3);

    latch.await(2, TimeUnit.SECONDS);
    assertEquals(result, 5);
  }

  @Test
  public void testPromiseAllReject() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);

    Promise p1 = new Promise();
    Promise p2 = new Promise();
    
    def result;

    Promise.all(p1, p2).then( { List l ->
      // not used for this test
    }, { Exception e ->
      result = e;
    });

    p1.fulfill(2);
    String reason = "Reject Reason";
    p2.reject(reason);

    latch.await(2, TimeUnit.SECONDS);
    assertTrue(result instanceof Exception);
    assertTrue(((Exception) result).getMessage().contains(reason));
  }



  private Promise<String> makePromise(String message) {
    Promise<String> p = new Promise()

    Thread.start {
      sleep 250
      p.fulfill message
    }

    return p
  }

  private Promise<String> makeRejection(String message) {
    Promise<String> p = new Promise()

    Thread.start {
      sleep 250
      p.reject 'Foobar'
    }

    return p
  }
  
  
}
