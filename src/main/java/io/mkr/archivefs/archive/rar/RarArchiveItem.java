package io.mkr.archivefs.archive.rar;

import com.github.junrar.rarfile.FileHeader;
import com.github.junrar.rarfile.HostSystem;
import io.mkr.archivefs.archive.ArchiveItemFlags;
import io.mkr.archivefs.archive.DelegatingArchiveItem;

public class RarArchiveItem extends DelegatingArchiveItem<FileHeader> {

  public RarArchiveItem(FileHeader fileHeader) {
    super(fileHeader);
  }

  @Override
  public String getPath() {
    return getDelegate().getFileNameW().equals("") ? getDelegate().getFileNameString() : getDelegate().getFileNameW();
  }

  @Override
  public long getCompressedSize() {
    return getDelegate().getFullPackSize();
  }

  @Override
  public long getSize() {
    return getDelegate().getFullUnpackSize();
  }

  @Override
  public boolean isDirectory() {
    return getDelegate().isDirectory();
  }

  @Override
  public ArchiveItemFlags getFlags() {
    return new ArchiveItemFlags.DEFAULT() {

      @Override
      public boolean isReadOnly() {
        return getDelegate().getHostOS().getHostByte() <= HostSystem.win32.getHostByte() &&
                (getDelegate().getFileAttr() & FA_FAT_READONLY) != 0;

      }

      @Override
      public boolean isHidden() {
        return getDelegate().getHostOS().getHostByte() <= HostSystem.win32.getHostByte() &&
                (getDelegate().getFileAttr() & FA_FAT_HIDDEN) != 0;
      }

      @Override
      public boolean isSystem() {
        return getDelegate().getHostOS().getHostByte() <= HostSystem.win32.getHostByte() &&
                (getDelegate().getFileAttr() & FA_FAT_SYSTEM) != 0;
      }

      @Override
      public boolean isArchiveFlag() {
        return getDelegate().getHostOS().getHostByte() <= HostSystem.win32.getHostByte() &&
                (getDelegate().getFileAttr() & FA_FAT_ARCHIVE) != 0;
      }

      @Override
      public boolean isVolumeLabel() {
        return getDelegate().getHostOS().getHostByte() <= HostSystem.win32.getHostByte() &&
                (getDelegate().getFileAttr() & FA_FAT_LABEL) != 0;
      }

    };
  }


}
