package io.mkr.archivefs.archive.lha;

import io.mkr.archivefs.archive.ArchiveItemFlags;
import io.mkr.archivefs.archive.DelegatingArchiveItem;
import net.sourceforge.lhadecompressor.LhaEntry;

public class LhaEntryArchiveItem extends DelegatingArchiveItem<LhaEntry> {

  public LhaEntryArchiveItem(LhaEntry delegate) {
    super(delegate);
  }

  @Override
  public String getPath() {
    String path = getDelegate().getFile().toString();
    return path.charAt(0) == '/' ? path : "/" + path;
  }

  @Override
  public long getCompressedSize() {
    return getDelegate().getCompressedSize();
  }

  @Override
  public long getSize() {
    return getDelegate().getOriginalSize();
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public ArchiveItemFlags getFlags() {
    // file attribute flags not implemented by LhaEntry
    return ArchiveItemFlags.EMPTY;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LhaEntry thatEntry = ((LhaEntryArchiveItem) o).getDelegate();
    return equalValues(getDelegate(), thatEntry);
  }

  private static boolean equalValues(LhaEntry entry1, LhaEntry entry2) {
    if (!entry1.getFile().toString().equals(entry2.getFile().toString())) return false;
    if (entry1.getOriginalSize() != entry2.getOriginalSize()) return false;
    if (entry1.getCRC() != entry2.getCRC()) return false;
    if (!entry1.getMethod().equals(entry2.getMethod())) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = getDelegate().getFile().toString().hashCode();
    result = 31 * result + (int) getDelegate().getOriginalSize();
    result = 31 * result + getDelegate().getCRC();
    result = 31 * result + getDelegate().getMethod().hashCode();
    return result;
  }
}
