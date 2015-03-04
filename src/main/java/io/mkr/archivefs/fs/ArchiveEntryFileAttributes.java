package io.mkr.archivefs.fs;

import io.mkr.archivefs.archive.ArchiveItem;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class ArchiveEntryFileAttributes implements BasicFileAttributes {

  private ArchiveItem archiveItem;

  public ArchiveEntryFileAttributes(ArchiveItem archiveItem) {
    this.archiveItem = archiveItem;
  }

  @Override
  public FileTime lastModifiedTime() {
    //todo
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
    return false;
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
    throw new UnsupportedOperationException("Not supported.");
  }


}
