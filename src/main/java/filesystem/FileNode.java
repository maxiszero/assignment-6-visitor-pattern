package filesystem;

import java.util.Objects;

/** Immutable in-memory file entry; no operating-system file is opened. */
public final class FileNode implements FileSystemElement {
    private final String name;
    private final long sizeBytes;

    public FileNode(String name, long sizeBytes) {
        this.name = ElementNames.requireValid(name);
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("File size must not be negative");
        }
        this.sizeBytes = sizeBytes;
    }

    @Override
    public String getName() {
        return name;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    @Override
    public void accept(FileSystemVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visitFile(this);
    }
}
