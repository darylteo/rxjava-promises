package com.darylteo.vertx.promises.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import rx.util.functions.FunctionLanguageAdaptor;
import rx.util.functions.Functions;

import com.darylteo.rx.promises.Promise;

public class JavascriptAdaptor implements FunctionLanguageAdaptor {

  public static void jsClassTester(Object jsObject) {
    Class<?> clazz = jsObject.getClass();
    while (clazz != Object.class) {
      System.out.println(clazz);

      for (Class<?> _interface : clazz.getInterfaces()) {
        System.out.println(_interface);
      }
      clazz = clazz.getSuperclass();
    }

  }

  public static boolean registerLanguageAdaptor() {
    String name = "javascript";
    String className = JavascriptAdaptor.class.getName();

    try {
      Class<?> c = JavascriptAdaptor.class;
      FunctionLanguageAdaptor a = (FunctionLanguageAdaptor) c.newInstance();
      Functions.registerLanguageAdaptor(a.getFunctionClass(), a);
      /*
       * Using System.err/System.out as this is the only place in the library
       * where we do logging and it's only at startup. I don't want to include
       * SL4J/Log4j just for this and no one uses Java Logging.
       */
      System.out.println("RxJava => Successfully loaded function language adaptor: " + name + " with path: " + className);
      return true;
    } catch (Exception e) {
      System.err.println("RxJava => Failed trying to initialize function language adaptor: " + className);
      e.printStackTrace();
      return false;
    }

  }

  @Override
  public Object call(Object function, Object[] args) {
    System.out.println("Hello World");
    NativeFunction func = ((NativeFunction) function);

    Context context = Context.enter();

    try {
      Scriptable scope = func.getParentScope();
      Scriptable that = context.newObject(scope);

      Object result = func.call(context, scope, that, args);
      if (NativeJavaObject.canConvert(result, Promise.class)) {
        return Context.jsToJava(result, Promise.class);
      }

      return result;
    } catch (JavaScriptException e) {
      // unchecked exception
      throw new RuntimeException(e.getCause());
    } finally {
      Context.exit();
    }
  }

  public Class<?>[] getFunctionClass() {

    return new Class<?>[] {
      org.mozilla.javascript.Function.class
    };
  }
}