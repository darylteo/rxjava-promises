package com.darylteo.promises;

public interface PromiseHandler<I, O> extends CompletedHandler<I> {
  public O handle(I value) throws Exception;
}
