package com.darylteo.vertx.promises.java.functions;

import rx.util.functions.Func1;

import com.darylteo.vertx.promises.java.Promise;

public abstract class RepromiseFunction<I, O> implements Func1<I, Promise<O>> {
}