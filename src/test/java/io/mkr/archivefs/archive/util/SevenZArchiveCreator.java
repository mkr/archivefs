package io.mkr.archivefs.archive.util;

import java.io.IOException;
import java.nio.file.Path;

public class SevenZArchiveCreator implements ArchiveCreator {

    @Override
    public void createFlatArchive(final Path path) throws IOException {
        ArchiveCreationHelper.createSevenZArchive(path);
    }
}
