package io.mkr.archivefs.archive;

/**
 * A factory interface
 * @param <T> the type
 */
public interface Creator<T> {

  public T create();

}
