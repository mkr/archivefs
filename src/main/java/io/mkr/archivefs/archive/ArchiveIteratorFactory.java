package io.mkr.archivefs.archive;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Provides {@link Iterator}s for an archive's metadata items and content.
 * @param <I>
 */
public interface ArchiveIteratorFactory<I extends InputStream> {

  /**
   * Returns an {@link Iterator} for the metadata of archive entries
   * @param stream the InputStream
   * @return an {@link Iterator} for the metadata of archive entries
   */
  public Iterator<ArchiveItem> itemIterator(final I stream);

  /**
   * Returns an {@link Iterator} for pairs of metadata and binary content for an entry in the archive
   * @param stream the InputStream
   * @return an {@link Iterator} for pairs of metadata and binary content for an entry in the archive
   */
  public Iterator<Map.Entry<ArchiveItem, byte[]>> itemAndContentIterator(final I stream);

}
