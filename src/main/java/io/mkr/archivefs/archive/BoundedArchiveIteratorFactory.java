package io.mkr.archivefs.archive;

import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An {@link ArchiveIteratorFactory} implementation which creates {@link Iterator}s that limit the maximum size
 * of content they read from an archive to {@link #maxContentBytes}.
 * @param <I>
 */
public class BoundedArchiveIteratorFactory<I extends InputStream> implements ArchiveIteratorFactory<I> {

  public static final long DEFAULT_MAX_CONTENT_BYTES = 1024L * 1024L * 1024L;

  private long maxContentBytes = DEFAULT_MAX_CONTENT_BYTES;

  private ArchiveItemReader<I> archiveItemReader;

  public BoundedArchiveIteratorFactory(ArchiveItemReader<I> archiveItemReader) {
    this.archiveItemReader = archiveItemReader;
  }

  @Override
  public Iterator<ArchiveItem> itemIterator(final I stream) {
    return new ReadAheadIterator<ArchiveItem>() {
      @Override
      public ArchiveItem readNext() throws IOException {
        return archiveItemReader.nextItem(stream);
      }
    };
  }

  @Override
  public Iterator<Map.Entry<ArchiveItem, byte[]>> itemAndContentIterator(final I stream) {
    return new ReadAheadIterator<Map.Entry<ArchiveItem, byte[]>>() {

      private long bytesRead = 0;
      private boolean contentSkipped = false;

      @Override
      public Map.Entry<ArchiveItem, byte[]> readNext() throws IOException {
        ArchiveItem item = archiveItemReader.nextItem(stream);
        if (item != null) {
          byte[] content = null;
          if (!contentSkipped) {
            // todo: better archive bomb prevention
            long proclaimedSize = item.getSize();
            if (proclaimedSize == -1) {
              // no size given in header, use special input stream to limit reading
              BoundedInputStream bis = new BoundedInputStream(stream, maxContentBytes - bytesRead);
              final ByteArrayOutputStream output = new ByteArrayOutputStream();
              final byte[] buffer = new byte[8192];
              int n;
              while (0 <= (n = bis.read(buffer))) {
                output.write(buffer, 0, n);
              }
              if (n != BoundedInputStream.BOUNDS_EXCEEDED) {
                content = output.toByteArray();
                bytesRead += content.length;
              } else {
                contentSkipped = true;
              }
            } else if (bytesRead + proclaimedSize < maxContentBytes) {
              // only read if still fits in boundaries
              content = new byte[(int) proclaimedSize];
              int read = IOUtils.readFully(stream, content);
              // check bytes actually read?
              bytesRead += read;
            } else {
              contentSkipped = true;
            }
          }
          return new AbstractMap.SimpleEntry<>(item, content);
        } else {
          return null;
        }
      }
    };
  }

  public long getMaxContentBytes() {
    return maxContentBytes;
  }

  public void setMaxContentBytes(long maxContentBytes) {
    this.maxContentBytes = maxContentBytes;
  }

  private static class BoundedInputStream extends InputStream {

    public static final int BOUNDS_EXCEEDED = -2;

    private InputStream in;
    private long bytesRemaining;

    public BoundedInputStream(InputStream in, long size) {
      this.in = in;
      this.bytesRemaining = size;
    }

    @Override
    public int read() throws IOException {
      if (bytesRemaining > 0) {
        --bytesRemaining;
        return in.read();
      } else {
        return BOUNDS_EXCEEDED;
      }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      if (len > bytesRemaining) {
        return BOUNDS_EXCEEDED;
      }
      final int bytesRead = in.read(b, off, len);
      if (bytesRead > 0) {
        bytesRemaining -= bytesRead;
      }
      return bytesRead;
    }

  }
}
