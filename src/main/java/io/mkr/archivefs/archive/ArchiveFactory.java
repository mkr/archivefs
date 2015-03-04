package io.mkr.archivefs.archive;

import io.mkr.archivefs.archive.cc.*;
import io.mkr.archivefs.archive.lha.LhaEntryArchiveItem;
import io.mkr.archivefs.archive.lha.LhaFileArchive;
import io.mkr.archivefs.archive.rar.RarFileArchive;
import net.sourceforge.lhadecompressor.LhaEntry;
import net.sourceforge.lhadecompressor.LhaInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.arj.ArjArchiveEntry;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * The central class that is able to create specific {@link Archive} instances for a variety of archive file formats.
 * There is support for specific formats being supplied as {@link File} instances while others are only supported
 * from {@link java.io.InputStream}.
 */
public class ArchiveFactory {

  public static final String MIME_ZIP = "application/zip";
  public static final String MIME_JAR = "application/java-archive";
  public static final String MIME_TAR =  "application/x-tar";
  public static final String MIME_TGZ =  "application/x-gtar";
  public static final String MIME_RAR = "application/x-rar-compressed";
  public static final String MIME_7Z = "application/x-7z-compressed";
  public static final String MIME_ARJ1 = "application/arj";
  public static final String MIME_ARJ2 = "application/x-arj";
  public static final String MIME_LZH1 = "application/x-lha";
  public static final String MIME_LZH2 = "application/x-lzh-compressed";
  public static final String MIME_LZH3 = "application/x-lharc";

  private static final Set<String> MIMES_FILE = new HashSet<>(
          Arrays.asList(MIME_ZIP, MIME_JAR, MIME_RAR, MIME_7Z, MIME_LZH1, MIME_LZH2,MIME_LZH3));

  private static final Set<String> MIMES_STREAM = new HashSet<>(
          Arrays.asList(MIME_ZIP, MIME_JAR, MIME_ARJ1, MIME_ARJ2, MIME_LZH1, MIME_LZH2,MIME_LZH3));


  public static boolean supportedFromFile(String mime) {
    return MIMES_FILE.contains(mime);
  }

  public static boolean supportedFromStream(String mime) {
    return MIMES_STREAM.contains(mime);
  }

  /**
   * Creates an {@link Archive} for a given {@link File}. Mostly, the returned instances is lean, i.e. doesn't
   * buffer large amounts but provides random-access by random direct disk access.
   * @param mimeType The mime type to create the archive for
   * @param file the {@link File}
   * @return the {@link Archive} or null if the type is not supported
   */
  public Archive archiveForFile(String mimeType, final File file) {
    switch (mimeType) {
      case MIME_ZIP:
      case MIME_JAR:
        return new CCZipFileArchive(file);
      case MIME_RAR:
        return new RarFileArchive(file);
      case MIME_7Z:
        // The 7Z file implementation does not allow random-access, map it to an in-memory archive
        return archiveForInputStream(mimeType, new Creator<InputStream>() {
          @Override
          public CCSeven7ArchiveInputStream create() {
            try {
              return new CCSeven7ArchiveInputStream(new SevenZFile(file));
            } catch (IOException e) {
              throw new IllegalArgumentException("Error accessing file", e);
            }
          }
        });
      case MIME_LZH1:
      case MIME_LZH2:
      case MIME_LZH3:
        return new LhaFileArchive(file);
    }
    return null;
  }

  public Archive archiveForInputStream(String mimeType, Creator<InputStream> streamCreator) {
    // create an in-memory archive first
    MemoryMappedArchive inMemoryArchive = inMemoryArchiveForInputStream(mimeType, streamCreator.create());
    if (inMemoryArchive != null) {
      // if archive content was (partially) skipped, fall back to streaming archive
      if (inMemoryArchive.hasSkippedContent()) {
        List<ArchiveItem> preparsedItems = null;
        try {
          preparsedItems = new ArrayList<>(inMemoryArchive.getItems());
        } catch (IOException e) {
          // cannot happen
        }
        return streamingArchiveForInputStream(mimeType, streamCreator, preparsedItems);
      }
    }
    return inMemoryArchive;
  }

  public MemoryMappedArchive inMemoryArchiveForInputStream(String mimeType, InputStream is) {
    switch (mimeType) {
      case MIME_ZIP:
        return new MemoryMappedArchive<>(new ZipArchiveInputStream(is),
                new BoundedArchiveIteratorFactory<>(new ZipEntryReader()));
      case MIME_JAR:
        return new MemoryMappedArchive<>(new JarArchiveInputStream(is),
                new BoundedArchiveIteratorFactory<>(new ZipEntryReader()));
      case MIME_TAR:
        return new MemoryMappedArchive<>(new TarArchiveInputStream(is),
                new BoundedArchiveIteratorFactory<>(new TarEntryReader()));
      case MIME_TGZ:
        try {
          return new MemoryMappedArchive<>(new TarArchiveInputStream(new GZIPInputStream(is)),
                  new BoundedArchiveIteratorFactory<>(new TarEntryReader()));
        } catch (IOException e) {
          throw new IllegalArgumentException("Error accessing TGZ", e);
        }
      case MIME_ARJ1:
      case MIME_ARJ2:
        try {
          return new MemoryMappedArchive<>(new ArjArchiveInputStream(is),
                  new BoundedArchiveIteratorFactory<>(new ArjEntryReader()));
        } catch (ArchiveException e) {
          throw new RuntimeException(e);
        }
      case MIME_LZH1:
      case MIME_LZH2:
      case MIME_LZH3:
        return new MemoryMappedArchive<>(new LhaInputStream(is),
                new BoundedArchiveIteratorFactory<>(new LhaEntryReader()));
      case MIME_7Z:
        return new MemoryMappedArchive<>((CCSeven7ArchiveInputStream) is,
                new BoundedArchiveIteratorFactory<>(new SevenZEntryReader()));
    }
    return null;
  }

  public Archive streamingArchiveForInputStream(String mimeType, Creator<InputStream> streamCreator,
                                                List<ArchiveItem> preparsedEntries) {
    switch (mimeType) {
      case MIME_ZIP:
        return new StreamingArchive<>(
                new DecoratingCreator<InputStream, ZipArchiveInputStream>(streamCreator) {
                  @Override
                  public ZipArchiveInputStream decorate(InputStream is) {
                    return new ZipArchiveInputStream(is);
                  }
                },
                new BoundedArchiveIteratorFactory<>(new ZipEntryReader()),
                preparsedEntries);
      case MIME_JAR:
        return new StreamingArchive<>(
                new DecoratingCreator<InputStream, JarArchiveInputStream>(streamCreator) {
                  @Override
                  public JarArchiveInputStream decorate(InputStream is) {
                    return new JarArchiveInputStream(is);
                  }
                },
                new BoundedArchiveIteratorFactory<>(new JarEntryReader()),
                preparsedEntries);
      case MIME_LZH1:
      case MIME_LZH2:
      case MIME_LZH3:
        return new StreamingArchive<>(
                new DecoratingCreator<InputStream, LhaInputStream>(streamCreator) {
                  @Override
                  public LhaInputStream decorate(InputStream is) {
                    return new LhaInputStream(is);
                  }
                },
                new BoundedArchiveIteratorFactory<>(new LhaEntryReader()),
                preparsedEntries);
      case MIME_7Z:
        return new StreamingArchive<>(
                new DecoratingCreator<InputStream, CCSeven7ArchiveInputStream>(streamCreator) {
                  @Override
                  public CCSeven7ArchiveInputStream decorate(InputStream o) {
                    return ((CCSeven7ArchiveInputStream) o);
                  }
                },
                new BoundedArchiveIteratorFactory<>(new SevenZEntryReader()),
                preparsedEntries);
    }
    return null;
  }

  public static class CCEntryReader implements ArchiveItemReader<ArchiveInputStream> {

    @Override
    public ArchiveItem nextItem(ArchiveInputStream input) throws IOException {
      ArchiveEntry entry = input.getNextEntry();
      return entry != null ? new CCArchiveEntryItem<>(entry) : null;
    }

  }

  public static class ZipEntryReader implements ArchiveItemReader<ZipArchiveInputStream> {

    @Override
    public CCZipArchiveEntryItem nextItem(ZipArchiveInputStream input) throws IOException {
      ZipArchiveEntry entry = input.getNextZipEntry();
      return entry != null ? new CCZipArchiveEntryItem(entry) : null;
    }
  }

  public static class JarEntryReader implements ArchiveItemReader<JarArchiveInputStream> {

    @Override
    public CCZipArchiveEntryItem nextItem(JarArchiveInputStream input) throws IOException {
      JarArchiveEntry entry = input.getNextJarEntry();
      return entry != null ? new CCZipArchiveEntryItem(entry) : null;
    }
  }

  public static class TarEntryReader implements ArchiveItemReader<TarArchiveInputStream> {

    @Override
    public CCTarArchiveEntryItem nextItem(TarArchiveInputStream input) throws IOException {
      TarArchiveEntry entry = input.getNextTarEntry();
      return entry != null ? new CCTarArchiveEntryItem(entry) : null;
    }
  }


  public static class ArjEntryReader implements ArchiveItemReader<ArjArchiveInputStream> {

    @Override
    public ArchiveItem nextItem(ArjArchiveInputStream input) throws IOException {
      ArjArchiveEntry arjEntry = input.getNextEntry();
      return arjEntry != null ? new CCArjArchiveEntryItem(arjEntry) : null;
    }
  }

  public static class SevenZEntryReader implements ArchiveItemReader<CCSeven7ArchiveInputStream> {

    @Override
    public ArchiveItem nextItem(CCSeven7ArchiveInputStream input) throws IOException {
      SevenZArchiveEntry sevenZEntry = input.getNextEntry();
      return sevenZEntry != null ? new CCArchiveEntryItem<>(sevenZEntry) : null;
    }
  }

  public static class LhaEntryReader implements ArchiveItemReader<LhaInputStream> {

    @Override
    public ArchiveItem nextItem(LhaInputStream input) throws IOException {
      LhaEntry lhaEntry = input.getNextEntry();
      return lhaEntry != null ? new LhaEntryArchiveItem(lhaEntry) : null;
    }

  }



}
