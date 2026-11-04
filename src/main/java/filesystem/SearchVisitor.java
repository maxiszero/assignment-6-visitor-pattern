package filesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Collects file and directory names containing a case-insensitive substring. */
public final class SearchVisitor implements FileSystemVisitor {
    private final String query;
    private final List<FileSystemElement> matches = new ArrayList<>();

    public SearchVisitor(String query) {
        Objects.requireNonNull(query, "query");
        if (query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }
        this.query = query.toLowerCase(Locale.ROOT);
    }

    @Override
    public void visitFile(FileNode file) {
        match(file);
    }

    @Override
    public void visitDirectory(DirectoryNode directory) {
        match(directory);
    }

    private void match(FileSystemElement element) {
        if (element.getName().toLowerCase(Locale.ROOT).contains(query)) {
            matches.add(element);
        }
    }

    public List<FileSystemElement> getMatches() {
        return List.copyOf(matches);
    }
}
