package io.mkr.archivefs.archive.rar;

import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import io.mkr.archivefs.archive.Archive;
import io.mkr.archivefs.archive.ArchiveItem;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RarFileArchive implements Archive, Closeable {

  private com.github.junrar.Archive archive;

  public RarFileArchive(File file) {
    try {
      archive = new com.github.junrar.Archive(file);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Collection<ArchiveItem> getItems() throws IOException {
    List<FileHeader> fileHeaders = archive.getFileHeaders();
    List<ArchiveItem> items = new ArrayList<>(fileHeaders.size());
    for (FileHeader fh: fileHeaders) {
      items.add(new RarArchiveItem(fh));
    }
    return items;
  }

  @Override
  public InputStream contentFor(ArchiveItem entry) throws IOException {
    FileHeader fh = ((RarArchiveItem) entry).getDelegate();
    if (fh.isSplitAfter() || fh.isSplitBefore()) {
      throw new IllegalArgumentException("Multi volume archives not supported");
    }
    try {
      return archive.getInputStream(fh);
    } catch (RarException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void close() throws IOException {
    archive.close();
  }
}
