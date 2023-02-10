package io.mkr.archivefs.archive.cc;

import io.mkr.archivefs.archive.util.ArchiveConstants;
import io.mkr.archivefs.archive.util.ArchiveCreator;
import io.mkr.archivefs.archive.util.SevenZArchiveCreator;
import io.mkr.archivefs.fs.ArchiveFilesystemProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlatArchiveTest {

    private static final ArchiveFilesystemProvider afsp = new ArchiveFilesystemProvider();

    @TempDir
    Path tempDir;

    private Path testFile;

    private static Stream<ArchiveCreator> archiveCreators() {
        return Stream.of(
                new SevenZArchiveCreator()
        );
    }

    @BeforeEach
    void createTempDirectory() {
        testFile = tempDir.resolve("testfile");
    }

    @ParameterizedTest
    @MethodSource("archiveCreators")
    void readContentFromFlatArchive(final ArchiveCreator archiveCreator) throws IOException {
        archiveCreator.createFlatArchive(testFile);
        assertTrue(Files.exists(testFile));

        try (final FileSystem afs = afsp.newFileSystem(testFile, new HashMap<>())) {
            final Path testFileInArchive = afs.getPath("/" + ArchiveConstants.TEST_FILE_NAME);
            assertAll(() -> assertTrue(Files.exists(testFileInArchive)),
                    () -> assertArrayEquals(ArchiveConstants.TEST_FILE_CONTENT.getBytes(StandardCharsets.UTF_8),
                            Files.readAllBytes(testFileInArchive)));
        }
    }

    @ParameterizedTest
    @MethodSource("archiveCreators")
    void accessFileSystemAfterClose(final ArchiveCreator archiveCreator) throws IOException {
        archiveCreator.createFlatArchive(testFile);
        assertTrue(Files.exists(testFile));

        final FileSystem afs = afsp.newFileSystem(testFile, new HashMap<>());
        final Path testFileInArchive = afs.getPath("/" + ArchiveConstants.TEST_FILE_NAME);
        afs.close();

        assertNotNull(afs.getPath("/" + ArchiveConstants.TEST_FILE_NAME));
        assertThrows(IOException.class, () -> Files.readAllBytes(testFileInArchive));
    }
}
