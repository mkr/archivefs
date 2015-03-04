package io.mkr.archivefs.fs;

import io.mkr.archivefs.archive.Archive;
import io.mkr.archivefs.archive.ArchiveFactory;
import io.mkr.archivefs.archive.Creator;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class ArchiveFilesystemProvider extends FileSystemProvider {

  public static final String URL_SCHEME = "archivefs";

  private static final String SEP_ARCHIVE = "!";

  private final Map<String, WeakReference<ArchiveFileSystem>> filesystems = new WeakHashMap<>();

  private MimeTypeDetector detector = new TikaMimeTypeDetector();

  private final ArchiveFactory archiveFactory = new ArchiveFactory();

  private static final URLCodec URLCODEC = new URLCodec();

  @Override
  public String getScheme() {
    return URL_SCHEME;
  }

  @Override
  public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    checkScheme(uri);
    // shortcut: if uri is known, return the fs directly
    ArchiveFileSystem fs = lookupCached(uri.toString());
    if (fs != null) {
      return fs;
    }
    return createFromURI(splitArchiveUri(uri), true);
  }

  @Override
  public FileSystem getFileSystem(URI uri) {
    checkScheme(uri);
    ArchiveFileSystem fs = lookupCached(uri.toString());
    if (fs != null) {
      return fs;
    } else {
      throw new FileSystemNotFoundException("No such file system: " + uri.toString());
    }
  }

  @Override
  public Path getPath(URI uri) {
    checkScheme(uri);
    // get the file system
    List<String> pathElements = splitArchiveUri(uri);
    ArchiveFileSystem fs;
    try {
      fs = createFromURI(pathElements, false);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    if (pathElements.size() == 1) {
      return fs.getRootPath();
    } else {
      return fs.getPath(pathElements.get(pathElements.size()-1));
    }
  }

  private ArchiveFileSystem createFromURI(List<String> pathElements, boolean allParts) throws IOException {
    // top level part of the URL should denote a file
    URI topContainerUri = URI.create(pathElements.get(0));
    if (!topContainerUri.getScheme().equals("file")) {
      throw new IllegalArgumentException("Only supporting 'file' scheme based top level archives.");
    }
    ArchiveFileSystem fs = lookupCached(topContainerUri.toString());
    if (fs == null) {
      fs = createFilebasedArchiveFs(topContainerUri);
      storeInCache(topContainerUri.toString(), fs);
    }

    // for each embedded archive create another file system
    StringBuilder embeddedPath = new StringBuilder();
    for (int i = 1; i < (allParts ? pathElements.size() : pathElements.size()-1); i++) {
      String localPath = pathElements.get(i);
      embeddedPath.append(SEP_ARCHIVE).append(localPath);
      URI archiveURI;
      try {
        archiveURI = new URI(topContainerUri.toString() + embeddedPath.toString());
      } catch (URISyntaxException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
      ArchiveFileSystem childFs = lookupCached(archiveURI.toString());
      if (childFs == null) {
        childFs = createEmbeddedArchiveFs(archiveURI, fs, localPath);
        storeInCache(archiveURI.toString(), childFs);
      }
      fs = childFs;
    }
    return fs;
  }

  private ArchiveFileSystem createFilebasedArchiveFs(URI fileUri) throws IOException {
    // detect archive type
    File archiveFile = new File(fileUri);
    String mime;
    try {
      mime = detector.detect(archiveFile);
    } catch (IOException e) {
      throw new IllegalArgumentException("Not an archive", e);
    }
    Archive archive = archiveFactory.archiveForFile(mime, archiveFile);
    if (archive == null) {
      throw new IllegalArgumentException("Not an archive");
    }
    return new ArchiveFileSystem(this, fileUri, archive);
  }

  private ArchiveFileSystem createEmbeddedArchiveFs(URI fileAndEmbeddedArchiveUri, final ArchiveFileSystem parentFs,
                                                    String pathInContainer) throws IOException {
    final ArchivePath path = new ArchivePath(parentFs, pathInContainer);
    String mime = detector.detect(parentFs.newInputStream(path));
    Archive archive = archiveFactory.archiveForInputStream(mime,
            new Creator<InputStream>() {
              @Override
              public InputStream create() {
                try {
                  return parentFs.newInputStream(path);
                } catch (IOException e) {
                  return null;
                }
              }
            });
    if (archive == null) {
      throw new IllegalArgumentException("Not an archive");
    }
    return new ArchiveFileSystem(this, fileAndEmbeddedArchiveUri, archive);
  }

  @Override
  public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
    if (FileSystems.getDefault().equals(path.getFileSystem())) {
      return createFilebasedArchiveFs(path.toUri());
    } else if (path instanceof ArchivePath) {
      ArchivePath archivePath = (ArchivePath) path;
      return createEmbeddedArchiveFs(path.toUri(), archivePath.getFileSystem(), archivePath.getLocalPath());
    }
    return null;
  }

  @Override
  public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
    throw new UnsupportedOperationException("Read only");
  }

  @Override
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
    ArchivePath archivePath = toArchivePath(path);
    ArchiveFileSystem fs = archivePath.getFileSystem();
    return fs.newByteChannel(archivePath, options, attrs);
  }

  @Override
  public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
    ArchivePath archivePath = toArchivePath(path);
    ArchiveFileSystem fs = archivePath.getFileSystem();
    return fs.newInputStream(archivePath, options);
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
    // delegate to path which delegates to fs to create new dir stream
    // fs should have the index which contains all info required for the dir stream
    // ...
    return toArchivePath(dir).newDirectoryStream(filter);
  }

  @Override
  public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
    throw new UnsupportedOperationException("Read only");
  }

  @Override
  public void delete(Path path) throws IOException {
    throw new UnsupportedOperationException("Read only");
  }

  @Override
  public void copy(Path source, Path target, CopyOption... options) throws IOException {
    throw new UnsupportedOperationException("Read only");
  }

  @Override
  public void move(Path source, Path target, CopyOption... options) throws IOException {
    throw new UnsupportedOperationException("Read only");
  }

  @Override
  public boolean isSameFile(Path path, Path path2) throws IOException {
    // todo
    return false;
  }

  @Override
  public boolean isHidden(Path path) throws IOException {
    // todo
    return false;
  }

  @Override
  public FileStore getFileStore(Path path) throws IOException {
    // todo
    return null;
  }

  @Override
  public void checkAccess(Path path, AccessMode... modes) throws IOException {
    // todo
  }

  @Override
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
    //todo
    return null;
  }

  @Override
  public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
      if (type == BasicFileAttributes.class || type == ArchiveItemFileAttributes.class) {
        return (A) toArchivePath(path).getFileAttributes();
      } else {
        throw new UnsupportedOperationException("Unsupported attribute class " + type.getName());
      }
  }

  @Override
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
    //todo
    return null;
  }

  @Override
  public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
    throw new UnsupportedOperationException("Read only");
  }

  private ArchiveFileSystem lookupCached(String uriPath) {
    WeakReference<ArchiveFileSystem> fsRef = filesystems.get(uriPath);
    return fsRef != null ? fsRef.get() : null;
  }

  private void storeInCache(String uriPath, ArchiveFileSystem afs) {
    filesystems.put(uriPath, new WeakReference<>(afs));
  }

  public static List<String> splitArchiveUri(URI uri) {
    // first string should be protocol:<schemespecificpart>, optional other string should be plain
    String[] parts = uri.getRawSchemeSpecificPart().split(SEP_ARCHIVE);
    List<String> result = new ArrayList<>();
    for (String part: parts) {
      try {
        result.add(URLCODEC.decode(part));
      } catch (DecoderException e) {
        throw new IllegalArgumentException("Could not decode " + part, e);
      }
    }
    return result;
  }

  static ArchivePath toArchivePath(Path path) {
    if (path == null) {
      throw new NullPointerException();
    }
    if (!(path instanceof ArchivePath)) {
      throw new ProviderMismatchException();
    }
    return (ArchivePath) path;
  }

  private void checkScheme(URI uri) {
    if ((uri.getScheme() == null) || (!uri.getScheme().equalsIgnoreCase(getScheme()))) {
      throw new IllegalArgumentException("URI scheme is not '" + getScheme() + "'");
    }
  }

  /**
   * Checks whether a {@link Path} can be the source of another {@link io.mkr.archivefs.fs.ArchiveFileSystem}.
   * @param path the path
   * @return true if the path is an {@link Archive} and can be used as an {@link io.mkr.archivefs.fs.ArchiveFileSystem}
   */
  public boolean maybeArchiveFs(Path path) throws IOException {
    boolean isFilePath = FileSystems.getDefault().equals(path.getFileSystem());
    boolean isArchivePath = path instanceof ArchivePath;
    if (isFilePath) {
      String mime = detector.detect(path.toFile());
      return ArchiveFactory.supportedFromFile(mime);
    } else if (isArchivePath) {
      InputStream is = newInputStream(path);
      String mime = detector.detect(is);
      try {
        is.close();
      } catch (IOException e) {
      }
      return ArchiveFactory.supportedFromStream(mime);
    }
    return false;
  }


}
