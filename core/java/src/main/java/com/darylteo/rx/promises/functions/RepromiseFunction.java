package com.darylteo.rx.promises.functions;

import com.darylteo.rx.promises.Promise;

import rx.util.functions.Func1;

public abstract class RepromiseFunction<I, O> implements Func1<I, Promise<O>> {
}