package com.darylteo.rx.promises.test;

import com.darylteo.rx.promises.java.Promise;
import com.darylteo.rx.promises.java.functions.*;
import org.junit.Test;
import rx.Observable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class PromiseTestsJava {

  @Test
  public void testDefer() throws Exception {
    Promise<String> promise = new Promise();

    assertNotNull(promise);
    assertTrue(promise instanceof Promise);
  }

  @Test
  public void testDefer2() throws Exception {
    Promise<String> promise = makePromise("Hello World");

    assertNotNull(promise != null);
    assertNotNull(promise instanceof Promise);
  }

  /* Basic handler */
  @Test
  public void testBasic() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<String> result = new Result<String>();

    makePromise("Hello World")
      .then(new PromiseAction<String>() {
        @Override
        public void call(String message) {
          result.value = message;
          latch.countDown();
        }
      });

    latch.await(2l, TimeUnit.SECONDS);
    assertEquals("Hello World", result.value);
  }

  /* Test of Handlers - return Value */
  @Test
  public void testChain1() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<String> result = new Result<String>();

    makePromise("Hello World")
      .then(new PromiseFunction<String, String>() {
        @Override
        public String call(String result) {
          return result.toUpperCase();
        }
      })
      .then(new PromiseAction<String>() {
        @Override
        public void call(String message) {
          result.value = message;
          latch.countDown();
        }
      });
    latch.await(2l, TimeUnit.SECONDS);
    assertEquals("HELLO WORLD", result.value);
  }

  /* Chain of handlers - return Promise */
  @Test
  public void testChain2() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<String> result = new Result<String>();

    makePromise("Hello World")
      .then(new RepromiseFunction<String, String>() {
        @Override
        public Promise<String> call(final String result) {
          return makePromise(result.toUpperCase());
        }
      })
      .then(new PromiseAction<String>() {
        @Override
        public void call(String message) {
          result.value = message;
          latch.countDown();
        }
      });
    latch.await(2l, TimeUnit.SECONDS);
    assertEquals("HELLO WORLD", result.value);
  }

  /* Chain of handlers - forwarding on */
  @Test
  public void testChain3() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<String> result = new Result<String>();

    makePromise("Hello World")
      .then(new RepromiseFunction<String, String>() {
        @Override
        public Promise<String> call(final String result) {
          return makePromise(result.toUpperCase());
        }
      })
      .fail(new PromiseFunction<Exception, String>() {
        @Override
        public String call(Exception e) {
          fail("This should not occur");
          return "fail";
        }
      })
      .then(new PromiseAction<String>() {
        @Override
        public void call(String message) {
          result.value = message;
          latch.countDown();
        }
      });

    latch.await(2l, TimeUnit.SECONDS);
    assertEquals("HELLO WORLD", result.value);
  }

  @Test
  public void testMultiple1() throws Exception {
    final CountDownLatch latch = new CountDownLatch(2);

    Promise<String> mainPromise = makePromise("Hello World");

    mainPromise.then(new PromiseAction<String>() {
      @Override
      public void call(String result) {
        System.out.println("Before");
        assertEquals(latch.getCount(), 2);
        latch.countDown();
      }
    });
    mainPromise.then(new PromiseAction<String>() {
      @Override
      public void call(String result) {
        System.out.println("After");
        assertEquals(latch.getCount(), 1);
        latch.countDown();
      }
    });

    latch.await(2l, TimeUnit.SECONDS);
  }

  /* Exception with then() handler */
  @Test
  public void testException1() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(20); // Exception
        }
      }).then(
      new PromiseAction<Character>() {
        @Override
        public void call(Character value) {
          fail("Promise not correctly calling failure handler when exception or rejection occurs");
        }
      },
      new PromiseAction<Exception>() {
        @Override
        public void call(Exception e) {
          assertEquals("Exception is not StringIndexOutOfBoundsException", StringIndexOutOfBoundsException.class, e.getClass());
          latch.countDown();
        }
      }
    );

    latch.await(2l, TimeUnit.SECONDS);
  }

  /* Exception with fail() handler */
  @Test
  public void testException2() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(20); // Exception
        }
      }).fail(
      new PromiseAction<Exception>() {
        @Override
        public void call(Exception e) {
          assertEquals("Exception is not StringIndexOutOfBoundsException", StringIndexOutOfBoundsException.class, e.getClass());
          latch.countDown();
        }
      }
    );

    latch.await(2l, TimeUnit.SECONDS);
  }

  /* Exception with fail() handler */
  @Test
  public void testException3() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);

    makePromise("Hello World")
      .then(
        new PromiseFunction<String, Character>() {
          @Override
          public Character call(String result) {
            return result.charAt(20); // Exception
          }
        },
        new PromiseAction<Exception>() {
          @Override
          public void call(Exception e) {
            fail("This rejection handler should not be called!");
          }
        }
      ).fail(
      new PromiseAction<Exception>() {
        @Override
        public void call(Exception e) {
          assertTrue(e instanceof StringIndexOutOfBoundsException);
          latch.countDown();
        }
      }
    );

    latch.await(2l, TimeUnit.SECONDS);
  }

  /* Exception with handler */
  @Test
  public void testException4() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(20); // Exception
        }
      }).then(
      new PromiseFunction<Character, String>() {
        @Override
        public String call(Character value) {
          fail("Promise not correctly calling failure handler when exception or rejection occurs");
          return "The Char is : " + value;
        }
      },
      new PromiseFunction<Exception, String>() {
        @Override
        public String call(Exception value) {
          flag.set(true);
          return null;
        }
      }
    ).then(new PromiseAction<String>() {
      @Override
      public void call(String value) {
        assertNull(value);
        assertTrue("FailureHandler was not called", flag.get());
        latch.countDown();
      }
    });

    latch.await(2l, TimeUnit.SECONDS);
  }

  @Test
  public void testException5() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(20); // Exception
        }
      })
      .then(new PromiseAction<Character>() {
        @Override
        public void call(Character value) {
          latch.countDown();
        }
      });

    Thread.sleep(2000);
    assertEquals("Promise did not fail property", latch.getCount(), 1);
  }

  /* Test exception passing to further promises */
  @Test
  public void testException7() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(20); // Exception
        }
      })
      .then(new PromiseAction<Character>() {
        @Override
        public void call(Character value) {
          flag.set(true);
        }
      })
      .fail(new PromiseAction<Exception>() {
        @Override
        public void call(Exception e) {
          latch.countDown();
        }
      });

    latch.await(2l, TimeUnit.SECONDS);
    assertFalse("Promise did not fail properly", flag.get());
  }

  /* Fin with basic */
  @Test
  public void testFinally1() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(0);
        }
      })
      .fin(new FinallyAction() {
        @Override
        public void call() {
          latch.countDown();
        }
      });

    latch.await(2l, TimeUnit.SECONDS);
  }

  /* Fin with Exception */
  @Test
  public void testFinally2() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(20);
        }
      })
      .fin(new FinallyAction() {
        @Override
        public void call() {
          latch.countDown();
        }
      });

    latch.await(2l, TimeUnit.SECONDS);
  }

  /* then() handler after fin() */
  @Test
  public void testFinally3() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<Character> result = new Result<Character>();
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(0);
        }
      })
      .fin(new FinallyAction() {
        @Override
        public void call() {
          flag.set(true);
        }
      })
      .then(new PromiseAction<Character>() {
        @Override
        public void call(Character value) {
          result.value = value;
          latch.countDown();
        }
      });

    latch.await(2l, TimeUnit.SECONDS);
    // value from promise must pass through
    // finally handler must fire
    // finally return value must be ignored
    assertEquals("Wrong value passed after calling finally", result.value, new Character('H'));
    assertTrue(flag.get());
  }

  /* then() handler after fin() with promise(void) */
  @Test
  public void testFinally4() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<Character> result = new Result<Character>();
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(0);
        }
      })
      .fin(new FinallyFunction<Void>() {
        @Override
        public Promise<Void> call() {
          final Promise<Void> promise = new Promise();

          makePromise("Foo Bar").then(new PromiseAction<String>() {
            @Override
            public void call(String t1) {
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
          result.value = value;
          latch.countDown();
        }
      });

    latch.await(5l, TimeUnit.SECONDS);

    // value from promise must pass through
    // finally handler must fire
    // finally return value must be ignored
    // then must fire only after finally has fulfilled promise
    assertEquals("Wrong value passed after calling finally", new Character('H'), result.value);
    assertTrue(flag.get());
  }

  /* then() handler after fin() with promise(string) */
  @Test
  public void testFinally5() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<Character> result = new Result<Character>();
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(0);
        }
      })
      .fin(new FinallyFunction<String>() {
        @Override
        public Promise<String> call() {
          final Promise<String> promise = new Promise();

          makePromise("Foo Bar").then(new PromiseAction<String>() {
            @Override
            public void call(String t1) {
              flag.set(true);
              promise.fulfill(t1);
            }
          });

          return promise;
        }
      })
      .then(new PromiseAction<Character>() {
        @Override
        public void call(Character value) {
          result.value = value;
          latch.countDown();
        }
      });

    latch.await(5l, TimeUnit.SECONDS);

    // make sure that the value returned from Finally is NOT passed through
    assertEquals("Wrong value passed after calling finally", new Character('H'), result.value);
    assertTrue(flag.get());
  }

  /* then() rejection after fin() */
  @Test
  public void testFinally6() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final Result<Exception> result = new Result<Exception>();
    final AtomicBoolean flag = new AtomicBoolean(false);

    makePromise("Hello World")
      .then(new PromiseFunction<String, Character>() {
        @Override
        public Character call(String result) {
          return result.charAt(20);
        }
      })
      .fin(new FinallyAction() {
        @Override
        public void call() {
          flag.set(true);
        }
      })
      .fail(new PromiseAction<Exception>() {
        @Override
        public void call(Exception reason) {
          System.out.println(reason);
          result.value = reason;
          latch.countDown();
        }
      });

    latch.await(2l, TimeUnit.SECONDS);
    System.out.println(result.value instanceof StringIndexOutOfBoundsException);
    assertEquals("Exception was not StringIndexOutOfBoundsException", StringIndexOutOfBoundsException.class, result.value.getClass());
    assertTrue(flag.get());
  }

  @Test
  public void testPrefilled() throws Exception {
    Promise<String> p = new Promise();

    p.fulfill("Hello World");

    p.then(new PromiseAction<String>() {
      @Override
      public void call(String value) {
        assertEquals(value, "Hello World");
      }
    });
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