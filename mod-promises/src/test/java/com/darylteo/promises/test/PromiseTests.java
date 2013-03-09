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
import org.vertx.java.core.Handler;
import org.vertx.java.core.SimpleHandler;
import org.vertx.testtools.TestVerticle;

import rx.util.functions.Action1;

import com.darylteo.promises.FailureHandler;
import com.darylteo.promises.Promise;
import com.darylteo.promises.PromiseHandler;
import com.darylteo.promises.RepromiseHandler;

public class PromiseTests extends TestVerticle {

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
        .then(new PromiseHandler<String, Void>() {
          @Override
          public Void handle(String result) {
            assertEquals(result, "Hello World", result);
            testComplete();
            return null;
          }
        });
  }

  /* Test of Handlers - return Value */
  @Test
  public void testChain1() throws Exception {
    makePromise("Hello World")
        .then(new PromiseHandler<String, String>() {
          @Override
          public String handle(String result) {
            return result.toUpperCase();
          }
        })
        .then(new PromiseHandler<String, Void>() {
          @Override
          public Void handle(String result) {
            assertEquals(result, "HELLO WORLD", result);
            testComplete();
            return null;
          }
        });
  }

  /* Chain of handlers - return Promise */
  @Test
  public void testChain2() throws Exception {
    makePromise("Hello World")
        .then(new RepromiseHandler<String, String>() {
          @Override
          public Promise<String> handle(final String result) {
            return makePromise("Foo Bar");
          }
        })
        .then(new PromiseHandler<String, Void>() {
          @Override
          public Void handle(String result) {
            assertEquals(result, "Foo Bar", result);
            testComplete();
            return null;
          }
        });
  }

  /* Chain of handlers - forwarding on */
  @Test
  public void testChain3() throws Exception {
    makePromise("Hello World")
        .then(new RepromiseHandler<String, String>() {
          @Override
          public Promise<String> handle(final String result) {
            return makePromise("Foo Bar");
          }
        })
        .fail(new FailureHandler<String>() {
          @Override
          public String handle(Exception e) {
            return "fail";
          }
        })
        .then(new PromiseHandler<String, Void>() {
          @Override
          public Void handle(String result) {
            assertEquals(result, "Foo Bar", result);
            testComplete();
            return null;
          }
        });
  }

  @Test
  public void testMultiple1() throws Exception {
    Promise<String> mainPromise = makePromise("Hello World");
    final CountDownLatch latch = new CountDownLatch(2);

    mainPromise.then(new PromiseHandler<String, Void>() {
      @Override
      public Void handle(String result) {
        System.out.println("Before");
        assertEquals(latch.getCount(), 2);
        latch.countDown();
        return null;
      }
    });
    mainPromise.then(new PromiseHandler<String, Void>() {
      @Override
      public Void handle(String result) {
        System.out.println("After");
        assertEquals(latch.getCount(), 1);
        testComplete();
        return null;
      }
    });
  }

  /* Exception with then() handler */
  @Test
  public void testException1() throws Exception {
    makePromise("Hello World")
        .then(new PromiseHandler<String, Character>() {
          @Override
          public Character handle(String result) {
            return result.charAt(20); // Exception
          }
        }).then(
            new PromiseHandler<Character, Void>() {
              @Override
              public Void handle(Character value) {
                fail("Promise not correctly calling failure handler when exception or rejection occurs");
                testComplete();
                return null;
              }
            },
            new FailureHandler<Void>() {
              @Override
              public Void handle(Exception e) {
                System.out.println(e);
                assertTrue("Exception is not StringIndexOutOfBoundsException", e instanceof StringIndexOutOfBoundsException);
                testComplete();
                return null;
              }
            }
        );
  }

  /* Exception with fail() handler */
  @Test
  public void testException2() throws Exception {
    makePromise("Hello World")
        .then(new PromiseHandler<String, Character>() {
          @Override
          public Character handle(String result) {
            return result.charAt(20); // Exception
          }
        }).fail(
            new FailureHandler<Void>() {
              @Override
              public Void handle(Exception e) {
                assertTrue(e instanceof StringIndexOutOfBoundsException);
                testComplete();
                return null;
              }
            }
        );
  }

  /* Exception with fail() handler */
  @Test
  public void testException3() throws Exception {
    makePromise("Hello World")
        .then(
            new PromiseHandler<String, Character>() {
              @Override
              public Character handle(String result) {
                return result.charAt(20); // Exception
              }
            },
            new FailureHandler<Character>() {
              @Override
              public Character handle(Exception e) {
                fail("This rejection handler should not be called!");
                testComplete();
                return null;
              }
            }
        ).fail(
            new FailureHandler<Void>() {
              @Override
              public Void handle(Exception e) {
                assertTrue(e instanceof StringIndexOutOfBoundsException);
                testComplete();
                return null;
              }
            }
        );
  }

  /* Exception with handler */
  @Test
  public void testException4() throws Exception {
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
        .then(new PromiseHandler<String, Character>() {
          @Override
          public Character handle(String result) {
            return result.charAt(20); // Exception
          }
        }).then(
            new PromiseHandler<Character, String>() {
              @Override
              public String handle(Character value) {
                fail("Promise not correctly calling failure handler when exception or rejection occurs");
                testComplete();
                return "The Char is : " + value;
              }
            },
            new FailureHandler<String>() {
              @Override
              public String handle(Exception value) {
                flag.set(true);
                return null;
              }
            }
        ).then(new PromiseHandler<String, Void>() {
          @Override
          public Void handle(String value) {
            assertNull(value);
            assertTrue("FailureHandler was not called", flag.get());
            testComplete();
            return null;
          }
        });
  }

  @Test
  public void testException5() throws Exception {
    makePromise("Hello World")
        .then(new PromiseHandler<String, Character>() {
          @Override
          public Character handle(String result) {
            return result.charAt(20); // Exception
          }
        })
        .then(new PromiseHandler<Character, Void>() {
          @Override
          public Void handle(Character value) {
            fail("Promise should not execute this due to exception");
            testComplete();
            return null;
          }
        });

    endLater();
  }

  /* Test exception passing to further promises */
  @Test
  public void testException7() throws Exception {
    makePromise("Hello World")
        .then(new PromiseHandler<String, Character>() {
          @Override
          public Character handle(String result) {
            return result.charAt(20); // Exception
          }
        })
        .then(new PromiseHandler<Character, Void>() {
          @Override
          public Void handle(Character value) {
            fail("Promise should not execute this due to exception");
            testComplete();
            return null;
          }
        })
        .fail(new FailureHandler<Void>() {
          @Override
          public Void handle(Exception e) {
            assertTrue(e instanceof StringIndexOutOfBoundsException);
            testComplete();
            return null;
          }
        });
  }

  /* Fin with basic */
  @Test
  public void testFinally1() throws Exception {
    makePromise("Hello World")
        .then(new PromiseHandler<String, Character>() {
          @Override
          public Character handle(String result) {
            return result.charAt(0);
          }
        })
        .fin(new PromiseHandler<Void, Void>() {
          @Override
          public Void handle(Void value) {
            testComplete();
            return null;
          }
        });
  }

  /* Fin with Exception */
  @Test
  public void testFinally2() throws Exception {
    makePromise("Hello World")
        .then(new PromiseHandler<String, Character>() {
          @Override
          public Character handle(String result) {
            return result.charAt(20);
          }
        })
        .fin(new PromiseHandler<Void, Void>() {
          @Override
          public Void handle(Void value) {
            testComplete();
            return null;
          }
        });
  }

  /* then() handler after fin() */
  @Test
  public void testFinally3() throws Exception {
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
        .then(new PromiseHandler<String, Character>() {
          @Override
          public Character handle(String result) {
            return result.charAt(0);
          }
        })
        .fin(new PromiseHandler<Void, String>() {
          @Override
          public String handle(Void value) {
            flag.set(true);
            return "HelloWorld";
          }
        })
        .then(new PromiseHandler<Character, Void>() {
          @Override
          public Void handle(Character value) {
            // value from promise must pass through
            // finally handler must fire
            // finally return value must be ignored
            assertEquals(value, new Character('H'));
            assertTrue(flag.get());
            testComplete();
            return null;
          }
        });
  }

  /* then() rejection after fin() */
  @Test
  public void testFinally4() throws Exception {
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
        .then(new PromiseHandler<String, Character>() {
          @Override
          public Character handle(String result) {
            return result.charAt(20);
          }
        })
        .fin(new PromiseHandler<Void, String>() {
          @Override
          public String handle(Void value) {
            flag.set(true);
            return "Finally!";
          }
        })
        .fail(new FailureHandler<Void>() {
          @Override
          public Void handle(Exception reason) {
            assertTrue(reason instanceof StringIndexOutOfBoundsException);
            assertTrue(flag.get());
            testComplete();
            return null;
          }
        });
  }

  @Test
  public void testPrefilled() throws Exception {
    Promise<String> p = Promise.defer();

    p.fulfill("Hello World");

    p.then(new PromiseHandler<String, Void>() {
      @Override
      public Void handle(String value) {
        assertEquals(value, "Hello World");
        testComplete();
        return null;
      }
    });
  }

  @Test
  public void testRxBasic() {
    makePromise("Hello World")
        .subscribe(new Action1<String>() {
          @Override
          public void call(String value) {
            assertEquals(value, "Hello World");
            testComplete();
          }
        });

  }

  private Promise<String> makePromise(final String message) {
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

  private void endLater() {
    vertx.setTimer(2l, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        testComplete();
      }
    });
  }
}