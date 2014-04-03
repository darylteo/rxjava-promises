package com.darylteo.vertx.promises.java.functions;

import com.darylteo.rx.promises.functions.Func1;
import com.darylteo.vertx.promises.java.Promise;

public interface RepromiseFunction<I, O> extends Func1<I, Promise<O>> {
}