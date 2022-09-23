package io.mkr.archivefs.fs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Iterator;

public class ArchivePath implements Path {

  private final ArchiveFileSystem fileSystem;
  private final String localPath;
  private final PathSegments pathSegments;

  public ArchivePath(final ArchiveFileSystem fileSystem, final String localPath) {
    this.fileSystem = fileSystem;
    this.localPath = localPath;
    this.pathSegments = new PathSegments(localPath);
  }

  @Override
  public ArchiveFileSystem getFileSystem() {
    return fileSystem;
  }

  @Override
  public boolean isAbsolute() {
    return localPath.length() > 0 && localPath.charAt(0) == ArchiveFileSystem.PATH_SEPARATOR_CHAR;
  }

  @Override
  public Path getRoot() {
    return isAbsolute() ? fileSystem.getRootPath() : null;
  }

  @Override
  public Path getFileName() {
    return fileSystem.getPath(localPath.substring(localPath.lastIndexOf('/') + 1));
  }

  @Override
  public Path getParent() {
    return null;  //Todo
  }

  @Override
  public int getNameCount() {
    return pathSegments.getNumEl()-1;
  }

  @Override
  public Path getName(final int index) {
    return null;  //Todo
  }

  @Override
  public Path subpath(final int beginIndex, final int endIndex) {
    return null;  //Todo
  }

  @Override
  public boolean startsWith(final Path other) {
    return false;  //Todo
  }

  @Override
  public boolean startsWith(final String other) {
    return false;  //Todo
  }

  @Override
  public boolean endsWith(final Path other) {
    return false;  //Todo
  }

  @Override
  public boolean endsWith(final String other) {
    return false;  //Todo
  }

  @Override
  public Path normalize() {
    return null;  //Todo
  }

  @Override
  public Path resolve(final Path other) {
    return null;  //Todo
  }

  @Override
  public Path resolve(final String other) {
    return null;  //Todo
  }

  @Override
  public Path resolveSibling(final Path other) {
    return null;  //Todo
  }

  @Override
  public Path resolveSibling(final String other) {
    return null;  //Todo
  }

  @Override
  public Path relativize(final Path other) {
    return null;  //Todo
  }

  @Override
  public URI toUri() {
    URI archiveUri = fileSystem.getArchiveUri();
    String uriPath = archiveUri.getScheme().equals(ArchiveFilesystemProvider.URL_SCHEME) ?
            archiveUri.getSchemeSpecificPart() :
            archiveUri.toString();
    try {
      return new URI(ArchiveFilesystemProvider.URL_SCHEME, uriPath + "!" + localPath, null);
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public Path toAbsolutePath() {
    return isAbsolute() ? this : new ArchivePath(fileSystem, ArchiveFileSystem.PATH_SEPARATOR + localPath);
  }

  @Override
  public Path toRealPath(final LinkOption... options) throws IOException {
    return this;
  }

  @Override
  public File toFile() {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>[] events, final WatchEvent.Modifier... modifiers) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchKey register(final WatchService watcher, final WatchEvent.Kind<?>... events) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<Path> iterator() {
    return null;  //Todo
  }

  @Override
  public int compareTo(final Path other) {
    return 0;  //Todo
  }

  public String getLocalPath() {
    return localPath;
  }

  @Override
  public String toString() {
    return localPath;
  }

  public DirectoryStream<Path> newDirectoryStream(final DirectoryStream.Filter<? super Path> filter) {
    return fileSystem.newDirectoryStream(this, filter);
  }

  public ArchiveEntryFileAttributes getFileAttributes() throws NoSuchFileException {
    return fileSystem.getFileAttributes(getLocalPath());
  }


}
