package com.darylteo.vertx.promises.java.functions;

import com.darylteo.vertx.promises.java.Promise;
import rx.functions.Func1;

public interface RepromiseFunction<I, O> extends Func1<I, Promise<O>> {
}