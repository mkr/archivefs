package io.mkr.archivefs.archive.cc;

import io.mkr.archivefs.archive.ArchiveItemFlags;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

public class CCZipArchiveEntryItem extends CCArchiveEntryItem<ZipArchiveEntry> {

  public CCZipArchiveEntryItem(ZipArchiveEntry delegate) {
    super(delegate);
  }

  @Override
  public long getCompressedSize() {
    return getDelegate().getCompressedSize();
  }

  @Override
  public ArchiveItemFlags getFlags() {
    return new ArchiveItemFlags.DEFAULT() {

      @Override
      public boolean isReadOnly() {
        return getDelegate().getPlatform() == ZipArchiveEntry.PLATFORM_FAT &&
                (getDelegate().getExternalAttributes() & FA_FAT_READONLY) != 0;
      }

      @Override
      public boolean isHidden() {
        return getDelegate().getPlatform() == ZipArchiveEntry.PLATFORM_FAT &&
                (getDelegate().getExternalAttributes() & FA_FAT_HIDDEN) != 0;
      }

      @Override
      public boolean isSystem() {
        return getDelegate().getPlatform() == ZipArchiveEntry.PLATFORM_FAT &&
                (getDelegate().getExternalAttributes() & FA_FAT_SYSTEM) != 0;
      }

      @Override
      public boolean isArchiveFlag() {
        return getDelegate().getPlatform() == ZipArchiveEntry.PLATFORM_FAT &&
                (getDelegate().getExternalAttributes() & FA_FAT_ARCHIVE) != 0;
      }

      @Override
      public boolean isVolumeLabel() {
        return getDelegate().getPlatform() == ZipArchiveEntry.PLATFORM_FAT &&
                (getDelegate().getExternalAttributes() & FA_FAT_LABEL) != 0;
      }

      @Override
      public boolean isSymLink() {
        return getDelegate().isUnixSymlink();
      }
    };
  }
}
