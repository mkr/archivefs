package io.mkr.archivefs.archive.cc;

import io.mkr.archivefs.archive.Archive;
import io.mkr.archivefs.archive.ArchiveItem;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wrapper for 7z archives. Commons-compress does support random acces on 7z archives since 1.20.
 */
public class CCSevenZArchive implements Archive {

  private SevenZFile sevenZFile;

  private boolean closed;

  /**
   * Constructor for a new 7z archive.
   * @param file File which is a 7z archive.
   */
  public CCSevenZArchive(final File file) throws IOException {
    this(file.toPath());
  }

  /**
   * Constructor for a new 7z archive.
   * @param file Path to file which is a 7z archive.
   */
  public CCSevenZArchive(final Path file) throws IOException {
    this.sevenZFile = new SevenZFile(Files.newByteChannel(file));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<ArchiveItem> getItems() throws IOException {
    checkArchiveClosed();
    final List<ArchiveItem> entriesMeta = new ArrayList<>();
    for (final SevenZArchiveEntry entry : sevenZFile.getEntries()) {
      entriesMeta.add(new CCArchiveEntryItem(entry));
    }
    return entriesMeta;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InputStream contentFor(ArchiveItem entry) throws IOException {
    checkArchiveClosed();
    return sevenZFile.getInputStream((SevenZArchiveEntry) ((CCArchiveEntryItem) entry).getDelegate());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      sevenZFile.close();
      sevenZFile = null;
    }
  }

  /**
   * Checks if the archive is already closed. An {@link IOException} is thrown if the archive is already closed.
   * @throws IOException if the archive is already closed
   */
  private void checkArchiveClosed() throws IOException {
    if (closed) {
      throw new IOException("Archive is already closed!");
    }
  }
}
