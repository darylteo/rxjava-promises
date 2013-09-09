package com.darylteo.rx.java.promises.functions;

import com.darylteo.rx.java.promises.Promise;

import rx.util.functions.Func1;

public abstract class RepromiseFunction<I, O> implements Func1<I, Promise<O>> {
}