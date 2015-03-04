package io.mkr.archivefs.archive;

/**
 * A {@link io.mkr.archivefs.archive.ArchiveItem} that delegates field-level access.
 */
public abstract class DelegatingArchiveItem<T> implements ArchiveItem {

  private T delegate;

  public DelegatingArchiveItem(T delegate) {
    this.delegate = delegate;
  }

  public T getDelegate() {
    return delegate;
  }
}
