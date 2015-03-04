package io.mkr.archivefs.archive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An {@link Archive} that stores all its item metadata and contents in memory. Upon creation an
 * {@link ArchiveIteratorFactory}'s {@link ArchiveIteratorFactory#itemAndContentIterator(java.io.InputStream)}
 * is queried to read the complete content of the archive.
 * @param <S>
 */
public class MemoryMappedArchive<S extends InputStream> implements Archive {

  private ArchiveIteratorFactory<S> asr;
  private Map<ArchiveItem, byte[]> contents = new LinkedHashMap<>();

  private int itemsStored = 0;
  private int itemContentStored = 0;
  private long totalItemContentSize = 0;
  private boolean skippedContent = false;
  private Exception readException = null;

  public MemoryMappedArchive(S stream, ArchiveIteratorFactory<S> asr) {
    this.asr = asr;
    consume(stream);
  }

  private void consume(S stream) {
    // unpack archive into memory
    Iterator<Map.Entry<ArchiveItem, byte[]>> it = asr.itemAndContentIterator(stream);
    while (it.hasNext()) {
      Map.Entry<ArchiveItem, byte[]> entry = it.next();
      contents.put(entry.getKey(), entry.getValue());
      itemsStored++;
      if (entry.getValue() == null) {
        skippedContent = true;
      } else {
        itemContentStored++;
        totalItemContentSize += entry.getValue().length;
      }
    }
    // store readException from iteration
    if (it instanceof ReadAheadIterator) {
      this.readException = ((ReadAheadIterator) it).getLastException();
    }
    // stream end has been reached, close stream
    try {
      stream.close();
    } catch (IOException e) {
    }
  }

  @Override
  public Collection<ArchiveItem> getItems() throws IOException {
    return contents.keySet();
  }

  @Override
  public InputStream contentFor(ArchiveItem entry) throws IOException {
    return new ByteArrayInputStream(contents.get(entry));
  }

  public int getItemsStored() {
    return itemsStored;
  }

  public int getItemContentStored() {
    return itemContentStored;
  }

  public long getTotalItemContentSize() {
    return totalItemContentSize;
  }

  public boolean hasSkippedContent() {
    return skippedContent;
  }

  public Exception getReadException() {
    return readException;
  }

}
