package io.mkr.archivefs.archive.util;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArchiveCreationHelper {

    /**
     * Create a 7z file.
     * @param path Path where the 7z file should be created
     * @throws IOException If creating the 7z file fails
     */
    public static void createSevenZArchive(final Path path) throws IOException {
        try (final SevenZOutputFile out = new SevenZOutputFile(path.toFile())) {
            final File inputFile = Files.createTempFile("SevenZBaseTemp", "").toFile();

            SevenZArchiveEntry entry = out.createArchiveEntry(inputFile, ArchiveConstants.TEST_FILE_NAME);
            out.putArchiveEntry(entry);
            out.write(ArchiveConstants.TEST_FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
            out.closeArchiveEntry();

            Files.deleteIfExists(inputFile.toPath());
        }
    }
}
