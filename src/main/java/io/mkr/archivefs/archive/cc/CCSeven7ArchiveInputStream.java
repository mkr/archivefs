package io.mkr.archivefs.archive.cc;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * The commons compress {@link org.apache.commons.compress.archivers.sevenz.SevenZFile} implementation does not allow
 * random access to the archive's entries. The API is very similar to the {@link org.apache.commons.compress.archivers.ArchiveInputStream}
 * implementations, so we provide a wrapper here.
 */
public class CCSeven7ArchiveInputStream extends InputStream {

  private SevenZFile sevenZFile;

  public CCSeven7ArchiveInputStream(SevenZFile sevenZFile) {
    this.sevenZFile = sevenZFile;
  }

  public SevenZArchiveEntry getNextEntry() throws IOException {
    return sevenZFile.getNextEntry();
  }

  @Override
  public int read() throws IOException {
    return sevenZFile.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return sevenZFile.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return sevenZFile.read(b, off, len);
  }

  @Override
  public void close() throws IOException {
    sevenZFile.close();
  }
}
