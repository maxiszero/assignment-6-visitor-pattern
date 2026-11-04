package filesystem;

import java.util.List;

/** Common tree API; a leaf has no children. */
public interface FileSystemElement {
    String getName();

    default List<FileSystemElement> getChildren() {
        return List.of();
    }

    void accept(FileSystemVisitor visitor);
}
