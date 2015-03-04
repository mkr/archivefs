package io.mkr.archivefs.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An {@link Archive} that stores item metadata and re-reads the archive whenever an item's content needs to be
 * accessed.
 * @param <S>
 */
public class StreamingArchive<S extends InputStream> implements Archive {

  private Creator<S> streamCreator;
  private ArchiveIteratorFactory<S> asr;
  private List<ArchiveItem> entries = new ArrayList<>();

  public StreamingArchive(Creator<S> creator, ArchiveIteratorFactory<S> asr, List<ArchiveItem> entries) {
    this.streamCreator = creator;
    this.asr = asr;
    if (entries != null) {
      this.entries = entries;
    } else {
      readEntries();
    }
  }

  public StreamingArchive(Creator<S> creator, ArchiveIteratorFactory<S> asr) {
    this(creator, asr, null);
  }

  private void readEntries() {
    // read the archive's meta data
    S stream = streamCreator.create();
    Iterator<ArchiveItem> it = asr.itemIterator(stream);
    try {
      while (it.hasNext()) {
        entries.add(it.next());
      }
      // close stream, it has to be re-created for subsequent content reads
    } finally {
      try {
        stream.close();
      } catch (IOException e) {
      }
    }
  }

  @Override
  public Collection<ArchiveItem> getItems() throws IOException {
    return entries;
  }

  @Override
  public InputStream contentFor(ArchiveItem entry) throws IOException {
    // re-create/re-open the stream
    S stream = streamCreator.create();
    Iterator<ArchiveItem> it = asr.itemIterator(stream);
    while (it.hasNext()) {
      if (entry.equals(it.next())) {
        // entry is reached so current state of the stream is returned
        return stream;
      }
    }
    return null;
  }

}
