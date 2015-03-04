package io.mkr.archivefs.archive.cc;

import io.mkr.archivefs.archive.Archive;
import io.mkr.archivefs.archive.ArchiveItem;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

public class CCZipFileArchive implements Archive, Closeable {

  private ZipFile zipFile;

  public CCZipFileArchive(Path path) {
    if (path.getFileSystem() != FileSystems.getDefault()) {
      throw new IllegalArgumentException("Only supporting file:// paths");
    }
    assignZipFile(path.toFile());
  }

  public CCZipFileArchive(File file) {
    assignZipFile(file);
  }

  private void assignZipFile(File file) {
    try {
      this.zipFile = new ZipFile(file);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error accessing file", e);
    }
  }

  @Override
  public Collection<ArchiveItem> getItems() throws IOException {
    List<ArchiveItem> entriesMeta = new ArrayList<ArchiveItem>();
    Enumeration<ZipArchiveEntry> e = zipFile.getEntries();
    while (e.hasMoreElements()) {
      entriesMeta.add(new CCZipArchiveEntryItem(e.nextElement()));
    }
    return entriesMeta;
  }

  @Override
  public InputStream contentFor(ArchiveItem entry) throws IOException {
    return zipFile.getInputStream(((CCZipArchiveEntryItem) entry).getDelegate());
  }

  @Override
  public void close() throws IOException {
    zipFile.close();
  }


}
