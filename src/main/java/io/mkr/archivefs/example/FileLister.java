package io.mkr.archivefs.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileLister {

  public static void main(String[] args) throws Exception {
    Path startPath = new File(args[0]).toPath();
    new FileAndArchiveTreeWalker(new Visitor(), 100).walk(startPath);
  }

  private static class Visitor implements FileAndArchiveVisitor<Path> {

    int depth = 0;

    @Override
    public FileVisitResult preVisitArchive(Path archivePath) throws IOException {
      msg(leftPad(depth, "[a] " + archivePath.toString()));
      depth++;
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitArchive(Path archivePath, IOException exc) throws IOException {
      depth--;
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      msg(leftPad(depth, "[d] " + dir.toString()));
      depth++;
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      msg(leftPad(depth, "  --" + file.toString()));
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      depth--;
      return FileVisitResult.CONTINUE;
    }

    private void msg(String s) {
      System.out.printf(leftPad(depth, s));
      System.out.print("\n");
    }
  }

  private static String leftPad(int n, String s) {
    if (n <= 0) {
      return s;
    }
    char[] pad = new char[n];
    for (int i = 0; i < n; i++) {
      pad[i] = ' ';
    }
    return new String(pad).concat(s);
  }

}
