package filesystem;

import java.util.List;

/** A shared fixture keeps the two demos consistent without duplicating the tree. */
public final class SampleFileSystem {
    private SampleFileSystem() { }

    public static FileSystemElement create() {
        return new DirectoryNode("project", List.of(
                new FileNode("README.txt", 1200),
                new DirectoryNode("src", List.of(
                        new FileNode("Main.java", 2400), new FileNode("notes.txt", 600))),
                new DirectoryNode("docs", List.of(new FileNode("guide.txt", 1800)))));
    }
}
