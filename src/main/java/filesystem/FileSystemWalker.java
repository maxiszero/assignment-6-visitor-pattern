package filesystem;

import java.util.Objects;

/** One pre-order traversal policy, independent of every concrete operation. */
public final class FileSystemWalker {
    public void walk(FileSystemElement element, FileSystemVisitor visitor) {
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(visitor, "visitor");
        element.accept(visitor);
        for (FileSystemElement child : element.getChildren()) {
            walk(child, visitor);
        }
    }
}
