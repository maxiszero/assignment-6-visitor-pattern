package filesystem;

import java.util.List;
import java.util.Objects;

/** Immutable directory snapshot; children are never exposed as a mutable list. */
public final class DirectoryNode implements FileSystemElement {
    private final String name;
    private final List<FileSystemElement> children;

    public DirectoryNode(String name, List<FileSystemElement> children) {
        this.name = ElementNames.requireValid(name);
        this.children = List.copyOf(children);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<FileSystemElement> getChildren() {
        return children;
    }

    @Override
    public void accept(FileSystemVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visitDirectory(this);
    }
}
