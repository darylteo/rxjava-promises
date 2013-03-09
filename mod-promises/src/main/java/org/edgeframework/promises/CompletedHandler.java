package org.edgeframework.promises;

interface CompletedHandler<I> {
  public Object handle(I value) throws Exception;
}
