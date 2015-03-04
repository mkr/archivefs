package io.mkr.archivefs.example;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;

/**
 * A {@link FileVisitor} extension which notifies about entry into and exit from archives.
 */
public interface FileAndArchiveVisitor<T> extends FileVisitor<T> {

  FileVisitResult preVisitArchive(T archivePath) throws IOException;

  FileVisitResult postVisitArchive(T archivePath, IOException exc) throws IOException;

}
