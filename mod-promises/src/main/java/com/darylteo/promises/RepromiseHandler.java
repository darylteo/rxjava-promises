package com.darylteo.promises;

public interface RepromiseHandler<I, O> extends CompletedHandler<I> {
  public Promise<O> handle(I value);
}
