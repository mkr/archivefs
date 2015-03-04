package io.mkr.archivefs.fs;

import io.mkr.archivefs.archive.ArchiveItem;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class ArchiveItemFileAttributes implements BasicFileAttributes {

  private ArchiveItem archiveItem;

  public ArchiveItemFileAttributes(ArchiveItem archiveItem) {
    this.archiveItem = archiveItem;
  }

  @Override
  public FileTime lastModifiedTime() {
    return null;
  }

  @Override
  public FileTime lastAccessTime() {
    return null;
  }

  @Override
  public FileTime creationTime() {
    return null;
  }

  @Override
  public boolean isRegularFile() {
    return !archiveItem.isDirectory();
  }

  @Override
  public boolean isDirectory() {
    return archiveItem.isDirectory();
  }

  @Override
  public boolean isSymbolicLink() {
    return archiveItem.getFlags().isSymLink();
  }

  @Override
  public boolean isOther() {
    return false;
  }

  @Override
  public long size() {
    return archiveItem.getSize();
  }

  @Override
  public Object fileKey() {
    return null;
  }

}
