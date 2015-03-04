package io.mkr.archivefs.archive.cc;

import io.mkr.archivefs.archive.ArchiveItemFlags;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;

public class CCTarArchiveEntryItem extends CCArchiveEntryItem<TarArchiveEntry> {

  public CCTarArchiveEntryItem(TarArchiveEntry delegate) {
    super(delegate);
  }

  @Override
  public ArchiveItemFlags getFlags() {

    return new ArchiveItemFlags.DEFAULT() {

      @Override
      public boolean isLink() {
        return getDelegate().isLink();
      }

      @Override
      public boolean isSymLink() {
        return getDelegate().isSymbolicLink();
      }
    };
  }
}
