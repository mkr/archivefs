package io.mkr.archivefs.archive.cc;

import io.mkr.archivefs.archive.ArchiveItemFlags;
import io.mkr.archivefs.archive.DelegatingArchiveItem;
import org.apache.commons.compress.archivers.ArchiveEntry;

public class CCArchiveEntryItem<T extends ArchiveEntry> extends DelegatingArchiveItem<T> {

  public CCArchiveEntryItem(T delegate) {
    super(delegate);
  }

  @Override
  public String getPath() {
    String p = getDelegate().getName();
    // assume backslash in path is a DOS or Windows path which we want to be slashified
    p = p.replace('\\', '/');
    if (!p.startsWith("/"))
      p = "/" + p;
    return p;
  }

  @Override
  public long getCompressedSize() {
    // not implemented by ArchiveEntry
    return -1;
  }

  @Override
  public long getSize() {
    return getDelegate().getSize();
  }

  @Override
  public boolean isDirectory() {
    return getDelegate().isDirectory();
  }

  public ArchiveItemFlags getFlags() {
    return ArchiveItemFlags.EMPTY;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return getDelegate().equals(((CCArchiveEntryItem) o).getDelegate());

  }

  @Override
  public int hashCode() {
    return getDelegate().hashCode();
  }
}
