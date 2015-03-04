package io.mkr.archivefs.fs;

import io.mkr.archivefs.archive.ArchiveItem;
import io.mkr.archivefs.archive.ArchiveItemFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ArchiveItemIndex {

  private Map<String, Node<ArchiveItem>> nodes = new TreeMap<>();

  public static ArchiveItemIndex buildIndex(Iterable<ArchiveItem> iterable) {
    ArchiveItemIndex idx = new ArchiveItemIndex();
    for (ArchiveItem item: iterable) {
      idx.add(item);
    }
    return idx;
  }

  public ArchiveItem get(String path) {
    Node<ArchiveItem> result = nodes.get(path);
    return result == null ? null : result.item;
  }

  public ArchiveItemIndex add(ArchiveItem item) {
    Node<ArchiveItem> parent = null;
    PathSegments path = new PathSegments(item.getPath());
    // not only add the item but check and if not exist add pseudo dir item for all items on the path to the item
    for (int i = 1; i <= path.getNumEl(); i++) {
      String subPath = path.pathTo(i);
      Node<ArchiveItem> node = nodes.get(subPath);
      if (node == null) {
        node = i == path.getNumEl() ? new Node<>(item, parent) : new Node<>(makeVirtualDir(subPath), parent);
        nodes.put(subPath, node);
      }
      if (parent != null && !parent.subnodes.contains(node)) {
        parent.subnodes.add(node);
      }
      parent = node;
    }
    return this;
  }

  public ArchiveItem parentOf(String path) {
    Node<ArchiveItem> node = nodes.get(path);
    return node != null && node.parent != null ? node.parent.item : null;
  }

  public List<ArchiveItem> childrenOf(String path) {
    Node<ArchiveItem> node = nodes.get(path);
    List<ArchiveItem> result = new ArrayList<>();
    if (node != null) {
      for (Node<ArchiveItem> n: node.subnodes) {
        result.add(n.item);
      }
    }
    return result;
  }

  public static ArchiveItem makeVirtualDir(final String path) {
    return makeVirtualItem(path, true);
  }

  public static ArchiveItem makeVirtualItem(final String path, final boolean isDirectory) {
    return new ArchiveItem() {

      @Override
      public String getPath() {
        return path;
      }

      @Override
      public long getCompressedSize() {
        return -1;
      }

      @Override
      public long getSize() {
        return -1;
      }

      @Override
      public boolean isDirectory() {
        return isDirectory;
      }

      @Override
      public ArchiveItemFlags getFlags() {
        return null;
      }

      @Override
      public int hashCode() {
        return path.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj) {
          return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
          return false;
        }
        return path.equals(((ArchiveItem) obj).getPath());
      }
    };
  }

  static class Node<T> {

    T item;
    Node<T> parent;
    List<Node<T>> subnodes;

    public Node(T item) {
      this.item = item;
      this.parent = null;
      this.subnodes = new ArrayList<>();
    }

    public Node(T item, Node<T> parent) {
      this(item);
      this.parent = parent;
    }

    public Node(T item, List<Node<T>> subnodes) {
      this(item);
      this.subnodes = subnodes;
    }

    public Node(T item, Node<T> parent, List<Node<T>> subnodes) {
      this(item, parent);
      this.subnodes = subnodes;
    }

    public void addSubNode(Node<T> node) {
      this.subnodes.add(node);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Node node = (Node) o;
      return item.equals(node.item);
    }

    @Override
    public int hashCode() {
      return item.hashCode();
    }
  }



}
