package io.mkr.archivefs.archive.lha;

import io.mkr.archivefs.archive.Archive;
import io.mkr.archivefs.archive.ArchiveItem;
import net.sourceforge.lhadecompressor.LhaEntry;
import net.sourceforge.lhadecompressor.LhaFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class LhaFileArchive implements Archive, Closeable {

  private LhaFile lhaFile;

  public LhaFileArchive(File file) {
    try {
      this.lhaFile = new LhaFile(file);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error accessing file", e);
    }
  }

  @Override
  public Collection<ArchiveItem> getItems() throws IOException {
    List<ArchiveItem> entriesMeta = new ArrayList<>();
    for (Iterator it = lhaFile.entryIterator(); it.hasNext();) {
      entriesMeta.add(new LhaEntryArchiveItem((LhaEntry) it.next()));
    }
    return entriesMeta;
  }

  @Override
  public InputStream contentFor(ArchiveItem entry) throws IOException {
    return lhaFile.getInputStream(((LhaEntryArchiveItem) entry).getDelegate());
  }

  @Override
  public void close() throws IOException {
    lhaFile.close();
  }

}
