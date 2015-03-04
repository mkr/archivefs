package io.mkr.archivefs.fs;

import java.util.ArrayList;
import java.util.List;

class PathSegments {

  String path;
  int numEl;
  List<Integer> seps;

  PathSegments(String path) {
    // always assume path is absolute
    this.path = path.charAt(0) == '/' ? path : "/" + path;
    this.numEl = 0;
    this.seps = new ArrayList<>();
    for (int i = 0; i < this.path.length(); i++) {
      char c = this.path.charAt(i);
      if (c == '/') {
        this.seps.add(i);
        this.numEl++;
      }
    }
    if (!isDir()) {
      this.numEl++;
    }
  }

  String pathFrom(int from) {
    if (from < 1 || from > numEl) {
      throw new ArrayIndexOutOfBoundsException("");
    }
    if (from == 1) {
      return path;
    }
    return path.substring(seps.get(from-2)+1);
  }

  String pathTo(int to) {
    if (to < 1 || to > numEl) {
      throw new ArrayIndexOutOfBoundsException("");
    }
    if (to == numEl) {
      return path;
    }
    return path.substring(0, seps.get(to-1)+1);
  }

  String subPath(int from, int to) {
    if (from < 1 || from > numEl || to < 1 || to > numEl) {
      throw new ArrayIndexOutOfBoundsException("");
    }
    if (from == 1 && to == numEl) {
      return path;
    }
    return path.substring(from == 1 ? 0 : seps.get(from-2)+1, seps.get(to-1)+1);
  }

  boolean isDir() {
    return this.path.charAt(this.path.length()-1) == '/';
  }

  String filename() {
    if (numEl < 2) {
      return null;
    }
    String result = pathFrom(numEl);
    return isDir() ? result.substring(0, result.indexOf('/')) : result;
  }

  int getNumEl() {
    return numEl;
  }



}
