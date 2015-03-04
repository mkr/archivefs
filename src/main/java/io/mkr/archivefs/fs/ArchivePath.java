package io.mkr.archivefs.fs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Iterator;

public class ArchivePath implements Path {

  private ArchiveFileSystem fileSystem;
  private String localPath;
  private PathSegments pathSegments;

  public ArchivePath(ArchiveFileSystem fileSystem, String localPath) {
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
    throw new UnsupportedOperationException();
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
  public Path getName(int index) {
    return null;  //Todo
  }

  @Override
  public Path subpath(int beginIndex, int endIndex) {
    return null;  //Todo
  }

  @Override
  public boolean startsWith(Path other) {
    return false;  //Todo
  }

  @Override
  public boolean startsWith(String other) {
    return false;  //Todo
  }

  @Override
  public boolean endsWith(Path other) {
    return false;  //Todo
  }

  @Override
  public boolean endsWith(String other) {
    return false;  //Todo
  }

  @Override
  public Path normalize() {
    return null;  //Todo
  }

  @Override
  public Path resolve(Path other) {
    return null;  //Todo
  }

  @Override
  public Path resolve(String other) {
    return null;  //Todo
  }

  @Override
  public Path resolveSibling(Path other) {
    return null;  //Todo
  }

  @Override
  public Path resolveSibling(String other) {
    return null;  //Todo
  }

  @Override
  public Path relativize(Path other) {
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
  public Path toRealPath(LinkOption... options) throws IOException {
    return this;
  }

  @Override
  public File toFile() {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<Path> iterator() {
    return null;  //Todo
  }

  @Override
  public int compareTo(Path other) {
    return 0;  //Todo
  }

  public String getLocalPath() {
    return localPath;
  }

  @Override
  public String toString() {
    return localPath;
  }

  public DirectoryStream<Path> newDirectoryStream(DirectoryStream.Filter<? super Path> filter) {
    return fileSystem.newDirectoryStream(this, filter);
  }

  public ArchiveEntryFileAttributes getFileAttributes() throws NoSuchFileException {
    return fileSystem.getFileAttributes(getLocalPath());
  }


}
