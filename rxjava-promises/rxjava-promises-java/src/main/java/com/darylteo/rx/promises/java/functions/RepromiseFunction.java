package com.darylteo.rx.promises.java.functions;

import com.darylteo.rx.promises.functions.Func1;
import com.darylteo.rx.promises.java.Promise;

public interface RepromiseFunction<I, O> extends Func1<I, Promise<O>> {
}