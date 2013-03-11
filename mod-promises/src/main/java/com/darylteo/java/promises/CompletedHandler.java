package com.darylteo.java.promises;

import rx.util.functions.Func1;

interface CompletedHandler<I> extends Func1<I, Object>{
  public Object handle(I value) throws Exception;
}
