package io.mkr.archivefs.archive;

import java.io.IOException;

/**
 * An ArchiveItemReader reads and returns meta data of an item in the archive and returns it as an
 * {@link ArchiveItem}).
 * @param <A> the type the implementation is able to read from
 */
public interface ArchiveItemReader<A> {

  public ArchiveItem nextItem(A input) throws IOException;

}
