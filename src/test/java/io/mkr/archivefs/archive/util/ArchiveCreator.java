package io.mkr.archivefs.archive.util;

import java.io.IOException;
import java.nio.file.Path;

public interface ArchiveCreator {

    void createFlatArchive(Path path) throws IOException;

}
