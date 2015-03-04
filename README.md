ArchiveFS
=========

A simple read-only Java NIO2 Filesystem implementation for archive files

Project Goals
* Wide archive support
* Support nested archives within archives seamlessly
* Avoid temp file creation, handle all access directly from disk or memory
* Simplicity over full support of all NIO2 features
* Read-only support is sufficient

Help
* see io.mkr.archivefs.example.FileLister on how to use

Used Libraries
* org.apache.commons:commons-compress for major archive formats
* com.github.junrar:junrar for RAR archives
* net.sourceforge.lhadecompressor for LHA archives

Similar Projects
* https://truevfs.java.net/
* http://commons.apache.org/proper/commons-vfs/
