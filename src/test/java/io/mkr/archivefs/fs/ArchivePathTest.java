package io.mkr.archivefs.fs;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchivePathTest {

    /**
     * Provider for the archive file system implementation.
     */
    private static final ArchiveFilesystemProvider afsp = new ArchiveFilesystemProvider();

    @Test
    void getFileName(@TempDir Path tempDir) throws IOException {
        final Path testFile = tempDir.resolve("test.zip");
        try (final ZipArchiveOutputStream out = new ZipArchiveOutputStream(Files.newOutputStream(testFile))) {
            final String content = "Test";
            final ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
            entry.setSize(content.length());
            out.putArchiveEntry(entry);
            out.write(content.getBytes(StandardCharsets.UTF_8));
            out.closeArchiveEntry();
        }

        try (final FileSystem afs = afsp.newFileSystem(testFile, new HashMap<>())) {
            final Path testFileInArchive = afs.getPath("/test.txt");
            final Path fileName = testFileInArchive.getFileName();
            assertAll(
                    () -> assertTrue(fileName instanceof ArchivePath),
                    () -> assertEquals("test.txt", fileName.toString())
            );
        }
    }

}