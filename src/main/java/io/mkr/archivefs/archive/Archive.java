package io.mkr.archivefs.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * An archive is package of directories and files and their associated metadata. Classes implementing this interface
 * provide non-sequential ("random") access to the contents of an archive.
 */
public interface Archive {

  /**
   * Returns metadata items for all entries in the archive
   * @return metadata items for all entries in the archive
   * @throws IOException
   */
  Collection<ArchiveItem> getItems() throws IOException;

  /**
   * Returns an {@link InputStream} to the binary content for the given {@link ArchiveItem} or null if the
   * {@link ArchiveItem} is not part of this archive.
   * @param entry the {@link ArchiveItem} to get the contents for
   * @return an {@link InputStream} of the item's contents or null the item is not part of this archive
   * @throws IOException
   */
  InputStream contentFor(ArchiveItem entry) throws IOException;

}
