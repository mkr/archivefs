package io.mkr.archivefs.archive.cc;

import io.mkr.archivefs.archive.ArchiveItemFlags;
import org.apache.commons.compress.archivers.arj.ArjArchiveEntry;

public class CCArjArchiveEntryItem extends CCArchiveEntryItem<ArjArchiveEntry> {

  public CCArjArchiveEntryItem(ArjArchiveEntry archiveEntry) {
    super(archiveEntry);
  }

  @Override
  public ArchiveItemFlags getFlags() {

    // this implementation assumes standard FAT/NTFS based file attributes

    return new ArchiveItemFlags.DEFAULT() {

      @Override
      public boolean isReadOnly() {
        return (getDelegate().getMode() & FA_FAT_READONLY) != 0;
      }

      @Override
      public boolean isHidden() {
        return (getDelegate().getMode() & FA_FAT_HIDDEN) != 0;
      }

      @Override
      public boolean isSystem() {
        return (getDelegate().getMode() & FA_FAT_SYSTEM) != 0;
      }

      @Override
      public boolean isArchiveFlag() {
        return (getDelegate().getMode() & FA_FAT_ARCHIVE) != 0;
      }

      @Override
      public boolean isVolumeLabel() {
        return (getDelegate().getMode() & FA_FAT_LABEL) != 0;
      }

    };
  }

  // custom equals and hashCode because ArjArchiveEntry does not implement them

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    CCArjArchiveEntryItem other = (CCArjArchiveEntryItem) obj;
    String myName = getPath();
    String otherName = other.getPath();
    if (myName == null) {
      if (otherName != null) {
        return false;
      }
    } else if (!myName.equals(otherName)) {
      return false;
    }
    return getDelegate().getMode() == other.getDelegate().getMode()
            && getDelegate().getSize() == other.getDelegate().getSize()
            && getDelegate().getHostOs() == other.getDelegate().getHostOs()
            && getDelegate().getLastModifiedDate().equals(other.getDelegate().getLastModifiedDate());
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }
}
