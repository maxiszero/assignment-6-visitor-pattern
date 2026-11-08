package filesystem;

/** Counts entries by kind, including the starting element. */
public final class ElementCountVisitor implements FileSystemVisitor {
    private int fileCount;
    private int directoryCount;

    @Override
    public void visitFile(FileNode file) {
        fileCount++;
    }

    @Override
    public void visitDirectory(DirectoryNode directory) {
        directoryCount++;
    }

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }
}
