package io.mkr.archivefs.archive;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Adapts a class that only has a next() but no hasNext() equivalent to the Iterator interface.
 */
public abstract class ReadAheadIterator<T> implements Iterator<T> {

  private T next = null;
  private Exception lastException = null;

  protected abstract T readNext() throws IOException;

  protected void advance() {
    try {
      next = readNext();
    } catch (IOException e) {
      next = null;
      lastException = e;
    }
  }

  @Override
  public boolean hasNext() {
    if (next == null) {
      advance();
    }
    return next != null;
  }

  @Override
  public T next() {
    if (next == null) {
      advance();
      if (next == null) {
        throw new NoSuchElementException();
      }
    }
    T retval = next;
    next = null;
    return retval;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public Exception getLastException() {
    return lastException;
  }
}
