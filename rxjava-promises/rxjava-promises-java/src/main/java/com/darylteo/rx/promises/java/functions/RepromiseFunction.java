package com.darylteo.rx.promises.java.functions;

import com.darylteo.rx.promises.java.Promise;

import rx.util.functions.Func1;

public interface RepromiseFunction<I, O> extends Func1<I, Promise<O>> {
}