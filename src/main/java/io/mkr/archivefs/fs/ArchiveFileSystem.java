package io.mkr.archivefs.fs;

import io.mkr.archivefs.archive.Archive;
import io.mkr.archivefs.archive.ArchiveItem;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

public class ArchiveFileSystem extends FileSystem {

  public static final char PATH_SEPARATOR_CHAR = '/';
  public static final String PATH_SEPARATOR = "/";

  private FileSystemProvider fsp;
  private URI archiveUri;
  private Archive archive;
  private boolean open;

  private final ArchiveItemIndex index;
  private final ArchivePath rootPath = new ArchivePath(this, PATH_SEPARATOR);
  private final Set<String> supportedFileAttributeViews = Collections.singleton("basic");

  public ArchiveFileSystem(FileSystemProvider provider, URI archiveUri, Archive archive) {
    this.fsp = provider;
    this.archiveUri = archiveUri;
    this.archive = archive;
    this.open = true;
    try {
      this.index = ArchiveItemIndex.buildIndex(archive.getItems());
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not initialize from archive", e);
    }
  }

  public DirectoryStream<Path> newDirectoryStream(ArchivePath path, DirectoryStream.Filter<? super Path> filter) {
    return new ArchiveDirectoryStream(path, filter);
  }

  public SeekableByteChannel newByteChannel(ArchivePath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
    ArchiveItem item = index.get(path.getLocalPath());
    if (item == null || item.isDirectory()) {
      throw new NoSuchFileException(path.getLocalPath());
    }
    final ReadableByteChannel rbc = Channels.newChannel(archive.contentFor(item));
    final long size = item.getSize();
    return new SeekableByteChannel() {
      long read = 0;
      public boolean isOpen() {
        return rbc.isOpen();
      }

      public long position() throws IOException {
        return read;
      }

      public SeekableByteChannel position(long pos) throws IOException {
        // todo: at least simple forward-skip by calling read()?
        throw new UnsupportedOperationException();
      }

      public int read(ByteBuffer dst) throws IOException {
        int n = rbc.read(dst);
        if (n > 0) {
          read += n;
        }
        return n;
      }

      public SeekableByteChannel truncate(long size) throws IOException {
        throw new NonWritableChannelException();
      }

      public int write (ByteBuffer src) throws IOException {
        throw new NonWritableChannelException();
      }

      public long size() throws IOException {
        return size;
      }

      public void close() throws IOException {
        rbc.close();
      }
    };
  }

  public InputStream newInputStream(ArchivePath path, OpenOption... options) throws IOException {
    ArchiveItem item = index.get(path.getLocalPath());
    if (item == null || item.isDirectory()) {
      throw new NoSuchFileException(path.getLocalPath());
    }
    return archive.contentFor(item);
  }

  public ArchiveEntryFileAttributes getFileAttributes(String path) throws NoSuchFileException {
    ArchiveItem archiveItem = index.get(path);
    if (archiveItem == null) {
      throw new NoSuchFileException(path);
    }
    return new ArchiveEntryFileAttributes(archiveItem);
  }

  @Override
  public FileSystemProvider provider() {
    return fsp;
  }

  @Override
  public void close() throws IOException {
    if (open && archive instanceof Closeable) {
      ((Closeable) archive).close();
      open = false;
      // todo: remove on afsp
      //provider().
    }
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public String getSeparator() {
    return PATH_SEPARATOR;
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    return Collections.singleton((Path) rootPath);
  }

  public Path getRootPath() {
    return rootPath;
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return supportedFileAttributeViews;
  }

  @Override
  public Path getPath(String first, String... more) {
    String path;
    if (more.length == 0) {
      path = first;
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append(first);
      for (String segment: more) {
        if (segment.length() > 0) {
          if (sb.length() > 0) {
            sb.append('/');
          }
          sb.append(segment);
        }
      }
      path = sb.toString();
    }
    return new ArchivePath(this, path);
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndPattern) {
    // todo
    return null;
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchService newWatchService() throws IOException {
    throw new UnsupportedOperationException();
  }

  public URI getArchiveUri() {
    return archiveUri;
  }

  private class ArchiveDirectoryStream implements DirectoryStream<Path> {

    private List<Path> children = new ArrayList<>();

    public ArchiveDirectoryStream(ArchivePath path, DirectoryStream.Filter<? super Path> filter) {
      List<ArchiveItem> items = index.childrenOf(path.getLocalPath());
      try {
        for (ArchiveItem item: items) {
          ArchivePath p = new ArchivePath(ArchiveFileSystem.this, item.getPath());
          if (filter == null || filter.accept(p)) {
            this.children.add(p);
          }
        }
      } catch (IOException e) {
        // todo
      }
    }

    @Override
    public Iterator<Path> iterator() {
      return children.iterator();
    }

    @Override
    public void close() throws IOException {
    }
  }

}
