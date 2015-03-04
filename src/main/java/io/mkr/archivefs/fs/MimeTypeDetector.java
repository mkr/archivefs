package io.mkr.archivefs.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface MimeTypeDetector {

  String detect(File f) throws IOException;

  String detect(InputStream is) throws IOException;

}
