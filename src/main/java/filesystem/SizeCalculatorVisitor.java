package filesystem;

/** Accumulates file-content bytes during one traversal; use a fresh instance per walk. */
public final class SizeCalculatorVisitor implements FileSystemVisitor {
    private long totalBytes;

    @Override
    public void visitFile(FileNode file) {
        totalBytes = Math.addExact(totalBytes, file.getSizeBytes());
    }

    @Override
    public void visitDirectory(DirectoryNode directory) {
        // Directories contribute no content bytes; the walker visits their files.
    }

    public long getTotalBytes() {
        return totalBytes;
    }
}
