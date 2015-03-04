package io.mkr.archivefs.archive;

/**
 * An ArchiveItem holds metadata about an item (file or directory) in the archive.
 */
public interface ArchiveItem {

  String getPath();

  long getCompressedSize();

  long getSize();

  boolean isDirectory();

  ArchiveItemFlags getFlags();

  //long getLastModified();

}
