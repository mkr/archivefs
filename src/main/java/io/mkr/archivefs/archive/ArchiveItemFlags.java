package io.mkr.archivefs.archive;

public interface ArchiveItemFlags {

  // FAT File Attribute Mask
  static final int FA_FAT_READONLY = 0x01;
  static final int FA_FAT_HIDDEN =	0x02;
  static final int FA_FAT_SYSTEM = 0x04;
  static final int FA_FAT_LABEL = 0x08;
  static final int FA_FAT_DIRECTORY	= 0x10;
  static final int FA_FAT_ARCHIVE = 0x20;

  boolean isReadOnly();

  boolean isHidden();

  boolean isSystem();

  boolean isArchiveFlag();

  boolean isVolumeLabel();

  boolean isLink();

  boolean isSymLink();

  class DEFAULT implements ArchiveItemFlags {
    @Override
    public boolean isReadOnly() {
      return false;
    }

    @Override
    public boolean isHidden() {
      return false;
    }

    @Override
    public boolean isSystem() {
      return false;
    }

    @Override
    public boolean isArchiveFlag() {
      return false;
    }

    @Override
    public boolean isVolumeLabel() {
      return false;
    }

    @Override
    public boolean isLink() {
      return false;
    }

    @Override
    public boolean isSymLink() {
      return false;
    }

  }

  static final ArchiveItemFlags EMPTY = new DEFAULT();

}
