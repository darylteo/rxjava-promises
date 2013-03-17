package com.darylteo.promises.test;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.fail;
import static org.vertx.testtools.VertxAssert.testComplete;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.vertx.java.core.SimpleHandler;

import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

import com.darylteo.promises.FinallyFunction;
import com.darylteo.promises.Promise;
import com.darylteo.promises.PromiseAction;
import com.darylteo.promises.PromiseFunction;

public class PromiseTests extends PromiseTestBase {

  @Test
  public void testDefer() throws Exception {
    Promise<String> promise = Promise.defer();

    assertNotNull(promise);
    assertTrue(promise instanceof Promise);
    testComplete();
  }

  @Test
  public void testDefer2() throws Exception {
    Promise<String> promise = makePromise("Hello World");

    assertNotNull(promise != null);
    assertNotNull(promise instanceof Promise);
    testComplete();
  }

  /* Basic handler */
  @Test
  public void testBasic() throws Exception {
    makePromise("Hello World")
        .then(new Action1<String>() {
          @Override
          public void call(String result) {
            assertEquals(result, "Hello World", result);
            testComplete();
          }
        });
  }

  /* Test of Handlers - return Value */
  @Test
  public void testChain1() throws Exception {
    makePromise("Hello World")
        .then(new Func1<String, String>() {
          @Override
          public String call(String result) {
            return result.toUpperCase();
          }
        })
        .then(new Action1<String>() {
          @Override
          public void call(String result) {
            assertEquals(result, "HELLO WORLD", result);
            testComplete();
          }
        });
  }

  /* Chain of handlers - return Promise */
  @Test
  public void testChain2() throws Exception {
    makePromise("Hello World")
        .then(new Func1<String, Promise<String>>() {
          @Override
          public Promise<String> call(final String result) {
            return makePromise(result + " Foo Bar");
          }
        })
        .then(new Action1<String>() {
          @Override
          public void call(String result) {
            assertEquals(result, "Hello World Foo Bar", result);
            testComplete();
          }
        });
  }

  /* Chain of handlers - forwarding on */
  @Test
  public void testChain3() throws Exception {
    makePromise("Hello World")
        .then(new Func1<String, Promise<String>>() {
          @Override
          public Promise<String> call(final String result) {
            return makePromise(result + " Foo Bar");
          }
        })
        .fail(new Func1<Exception, String>() {
          @Override
          public String call(Exception e) {
            fail("This should not occur");
            return "fail";
          }
        })
        .then(new Action1<String>() {
          @Override
          public void call(String result) {
            assertEquals(result, "Hello World Foo Bar", result);
            testComplete();
          }
        });
  }

  @Test
  public void testMultiple1() throws Exception {
    Promise<String> mainPromise = makePromise("Hello World");
    final CountDownLatch latch = new CountDownLatch(2);

    mainPromise.then(new Action1<String>() {
      @Override
      public void call(String result) {
        System.out.println("Before");
        assertEquals(latch.getCount(), 2);
        latch.countDown();
      }
    });
    mainPromise.then(new Action1<String>() {
      @Override
      public void call(String result) {
        System.out.println("After");
        assertEquals(latch.getCount(), 1);
        testComplete();
      }
    });
  }

  /* Exception with then() handler */
  @Test
  public void testException1() throws Exception {
    makePromise("Hello World")
        .then(new Func1<String, Character>() {
          @Override
          public Character call(String result) {
            return result.charAt(20); // Exception
          }
        }).then(
            new Action1<Character>() {
              @Override
              public void call(Character value) {
                fail("Promise not correctly calling failure handler when exception or rejection occurs");
                testComplete();
              }
            },
            new Action1<Exception>() {
              @Override
              public void call(Exception e) {
                System.out.println(e);
                assertTrue("Exception is not StringIndexOutOfBoundsException", e instanceof StringIndexOutOfBoundsException);
                testComplete();
              }
            }
        );
  }

  /* Exception with fail() handler */
  @Test
  public void testException2() throws Exception {
    makePromise("Hello World")
        .then(new Func1<String, Character>() {
          @Override
          public Character call(String result) {
            return result.charAt(20); // Exception
          }
        }).fail(
            new Action1<Exception>() {
              @Override
              public void call(Exception e) {
                assertTrue(e instanceof StringIndexOutOfBoundsException);
                testComplete();
              }
            }
        );
  }

  /* Exception with fail() handler */
  @Test
  public void testException3() throws Exception {
    makePromise("Hello World")
        .then(
            new Func1<String, Character>() {
              @Override
              public Character call(String result) {
                return result.charAt(20); // Exception
              }
            },
            new Func1<Exception, Character>() {
              @Override
              public Character call(Exception e) {
                fail("This rejection handler should not be called!");
                testComplete();
                return null;
              }
            }
        ).fail(
            new Action1<Exception>() {
              @Override
              public void call(Exception e) {
                assertTrue(e instanceof StringIndexOutOfBoundsException);
                testComplete();
              }
            }
        );
  }

  /* Exception with handler */
  @Test
  public void testException4() throws Exception {
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
        .then(new Func1<String, Character>() {
          @Override
          public Character call(String result) {
            return result.charAt(20); // Exception
          }
        }).then(
            new Func1<Character, String>() {
              @Override
              public String call(Character value) {
                fail("Promise not correctly calling failure handler when exception or rejection occurs");
                testComplete();
                return "The Char is : " + value;
              }
            },
            new Action1<Exception>() {
              @Override
              public void call(Exception value) {
                flag.set(true);
              }
            }
        ).then(new Action1<String>() {
          @Override
          public void call(String value) {
            assertNull(value);
            assertTrue("FailureHandler was not called", flag.get());
            testComplete();
          }
        });
  }

  @Test
  public void testException5() throws Exception {
    makePromise("Hello World")
        .then(new Func1<String, Character>() {
          @Override
          public Character call(String result) {
            return result.charAt(20); // Exception
          }
        })
        .then(new Action1<Character>() {
          @Override
          public void call(Character value) {
            fail("Promise should not execute this due to exception");
            testComplete();
          }
        });

    endLater();
  }

  /* Test exception passing to further promises */
  @Test
  public void testException7() throws Exception {
    makePromise("Hello World")
        .then(new Func1<String, Character>() {
          @Override
          public Character call(String result) {
            return result.charAt(20); // Exception
          }
        })
        .then(new Action1<Character>() {
          @Override
          public void call(Character value) {
            fail("Promise should not execute this due to exception");
            testComplete();
          }
        })
        .fail(new Action1<Exception>() {
          @Override
          public void call(Exception e) {
            assertTrue(e instanceof StringIndexOutOfBoundsException);
            testComplete();
          }
        });
  }

  /* Fin with basic */
  @Test
  public void testFinally1() throws Exception {
    makePromise("Hello World")
        .then(new Func1<String, Character>() {
          @Override
          public Character call(String result) {
            return result.charAt(0);
          }
        })
        .fin(new Action0() {
          @Override
          public void call() {
            testComplete();
          }
        });
  }

  /* Fin with Exception */
  @Test
  public void testFinally2() throws Exception {
    makePromise("Hello World")
        .then(new Func1<String, Character>() {
          @Override
          public Character call(String result) {
            return result.charAt(20);
          }
        })
        .fin(new Action0() {
          @Override
          public void call() {
            testComplete();
          }
        });
  }

  /* then() handler after fin() */
  @Test
  public void testFinally3() throws Exception {
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
        .then(new Func1<String, Character>() {
          @Override
          public Character call(String result) {
            return result.charAt(0);
          }
        })
        .fin(new Action0() {
          @Override
          public void call() {
            flag.set(true);
          }
        })
        .then(new Action1<Character>() {
          @Override
          public void call(Character value) {
            // value from promise must pass through
            // finally handler must fire
            // finally return value must be ignored
            assertEquals(value, new Character('H'));
            assertTrue(flag.get());
            testComplete();
          }
        });
  }

  /* then() handler after fin() */
  @Test
  public void testFinally4() throws Exception {
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
        .then(new PromiseFunction<String, Character>() {
          @Override
          public Character call(String result) {
            System.out.println(result);
            return result.charAt(0);
          }
        })
        .fin(new FinallyFunction<Void>() {
          @Override
          public Promise<Void> call() {
            final Promise<Void> promise = Promise.defer();

            vertx.runOnLoop(new SimpleHandler() {
              @Override
              protected void handle() {
                flag.set(true);
                promise.fulfill(null);
              }
            });

            return promise;
          }
        })
        .then(new PromiseAction<Character>() {
          @Override
          public void call(Character value) {
            // value from promise must pass through
            // finally handler must fire
            // finally return value must be ignored
            // then must fire only after finally has fulfilled promise
            assertEquals(value, new Character('H'));
            assertTrue(flag.get());
            testComplete();
          }
        });
  }

  /* then() rejection after fin() */
  @Test
  public void testFinally5() throws Exception {
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
        .then(new Func1<String, Character>() {
          @Override
          public Character call(String result) {
            return result.charAt(20);
          }
        })
        .fin(new Action0() {
          @Override
          public void call() {
            flag.set(true);
          }
        })
        .fail(new Action1<Exception>() {
          @Override
          public void call(Exception reason) {
            assertTrue(reason instanceof StringIndexOutOfBoundsException);
            assertTrue(flag.get());
            testComplete();
          }
        });
  }

  @Test
  public void testPrefilled() throws Exception {
    Promise<String> p = Promise.defer();

    p.fulfill("Hello World");

    p.then(new Action1<String>() {
      @Override
      public void call(String value) {
        assertEquals(value, "Hello World");
        testComplete();
      }
    });
  }
}