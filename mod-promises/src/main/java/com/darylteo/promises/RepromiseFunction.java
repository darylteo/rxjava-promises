package com.darylteo.promises;

import rx.util.functions.Func1;


public interface RepromiseFunction<I, O> extends Func1<I, Promise<O>> {
}