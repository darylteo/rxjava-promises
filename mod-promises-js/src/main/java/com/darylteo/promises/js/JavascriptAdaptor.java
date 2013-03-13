package com.darylteo.promises.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import rx.util.functions.FunctionLanguageAdaptor;

import com.darylteo.promises.Promise;

public class JavascriptAdaptor implements FunctionLanguageAdaptor {

  public static void registerLanguageAdaptor() {
    rx.util.functions.Functions.registerLanguageAdaptor(new Class<?>[] { NativeFunction.class }, new JavascriptAdaptor());
  }

  @Override
  public Object call(Object function, Object[] args) {
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
    return new Class<?>[] { NativeFunction.class };
  }
}