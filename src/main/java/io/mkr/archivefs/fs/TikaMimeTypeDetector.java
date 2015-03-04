package io.mkr.archivefs.fs;

import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TikaMimeTypeDetector implements MimeTypeDetector {

  private Tika tika = new Tika();

  @Override
  public String detect(File f) throws IOException {
    return tika.detect(f);
  }

  @Override
  public String detect(InputStream is) throws IOException {
    return tika.detect(is);
  }
}
