package filesystem;

/** One operation entry point for each concrete element type. */
public interface FileSystemVisitor {
    void visitFile(FileNode file);

    void visitDirectory(DirectoryNode directory);
}
