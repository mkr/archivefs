package io.mkr.archivefs.example;

import io.mkr.archivefs.fs.ArchiveFileSystem;
import io.mkr.archivefs.fs.ArchiveFilesystemProvider;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

/**
 * Very simple file tree walker which does not follow links but allows traversing archives.
 *
 * @see java.nio.file.FileTreeWalker
 */
public class FileAndArchiveTreeWalker {

  private final ArchiveFilesystemProvider afsp = new ArchiveFilesystemProvider();
  private final FileAndArchiveVisitor<? super Path> visitor;
  private final int maxDepth;

  public FileAndArchiveTreeWalker(FileAndArchiveVisitor<? super Path> visitor, int maxDepth) {
    this.visitor = visitor;
    this.maxDepth = maxDepth;
  }

  /**
   * Walk file tree starting at the given path
   * @param start the start path
   */
  public void walk(Path start) throws IOException {
    walk(start, 0);
  }

  private FileVisitResult walk(Path path, int depth) throws IOException {
    BasicFileAttributes attrs = null;
    try {
      try {
        attrs = path.getFileSystem().provider().readAttributes(path, BasicFileAttributes.class);
      } catch (IOException e) {
        return visitor.visitFileFailed(path, e);
      }
    } catch (SecurityException x) {
      // Need access to starting path, lack of access to all others is ignored
      if (depth == 0) {
        throw x;
      }
      return FileVisitResult.CONTINUE;
    }

    // visit file (and possibly archive)
    if (depth >= maxDepth || !attrs.isDirectory()) {
      FileVisitResult result = visitor.visitFile(path, attrs);
      if (result == FileVisitResult.CONTINUE) {
        // check if file is an archive
        if (depth < maxDepth && afsp.maybeArchiveFs(path)) {
          result = visitor.preVisitArchive(path);
          if (result == FileVisitResult.CONTINUE) {
            IOException ioex = null;
            try (ArchiveFileSystem afs = (ArchiveFileSystem) afsp.newFileSystem(path, new HashMap<String, Object>())) {
              Path rootInArchive = afs.getPath("/");
              walk(rootInArchive, depth + 1);
            } catch (IOException e) {
              ioex = e;
            }
            return visitor.postVisitArchive(path, ioex);
          }
        }
      }
      return result;
    }

    DirectoryStream<Path> stream = null;
    FileVisitResult result;

    // open the directory
    try {
      stream = Files.newDirectoryStream(path);
    } catch (IOException x) {
      return visitor.visitFileFailed(path, x);
    } catch (SecurityException x) {
      return FileVisitResult.CONTINUE;
    }

    // the exception notified to the postVisitDirectory method
    IOException ioex = null;

    try {
      result = visitor.preVisitDirectory(path, attrs);
      if (result != FileVisitResult.CONTINUE) {
        return result;
      }
      try {
        for (Path entry : stream) {
          result = walk(entry, depth + 1);
          if (result == null || result == FileVisitResult.TERMINATE) {
            return result;
          }
          // skip remaining siblings in this directory
          if (result == FileVisitResult.SKIP_SIBLINGS) {
            break;
          }
        }
      } catch (DirectoryIteratorException e) {
        ioex = e.getCause();
      }
    } finally {
      try {
        stream.close();
      } catch (IOException e) {
        if (ioex == null) {
          ioex = e;
        }
      }
    }
    return visitor.postVisitDirectory(path, ioex);
  }

}
