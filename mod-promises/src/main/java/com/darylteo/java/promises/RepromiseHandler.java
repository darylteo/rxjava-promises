package com.darylteo.java.promises;

import com.darylteo.promises.Promise;

public interface RepromiseHandler<I, O> extends CompletedHandler<I> {
  public Promise<O> handle(I value);
}
