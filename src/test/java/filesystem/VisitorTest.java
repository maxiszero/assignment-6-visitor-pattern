package filesystem;

import java.util.ArrayList;
import java.util.List;

/** Dependency-free behavioral tests, with assertions enabled without -ea. */
public final class VisitorTest {
    private static int passed;

    private VisitorTest() { }

    public static void main(String[] args) throws Exception {
        run("accept selects the matching visitor method through interface references", () -> {
            RecordingVisitor recorder = new RecordingVisitor();
            FileSystemVisitor visitor = recorder;
            FileSystemElement file = new FileNode("readme.txt", 20);
            FileSystemElement directory = new DirectoryNode("docs", List.of());
            file.accept(visitor);
            directory.accept(visitor);
            equal(List.of("file:readme.txt", "directory:docs"), recorder.events);
        });
        run("walker visits the root and every descendant exactly once in order", () -> {
            FileSystemElement root = new DirectoryNode("project", List.of(
                    new FileNode("a.txt", 10),
                    new DirectoryNode("src", List.of(new FileNode("b.java", 20)))));
            RecordingVisitor visitor = new RecordingVisitor();
            new FileSystemWalker().walk(root, visitor);
            equal(List.of("directory:project", "file:a.txt", "directory:src", "file:b.java"),
                    visitor.events);
        });
        run("directory snapshots input children and exposes a read-only list", () -> {
            List<FileSystemElement> children = new ArrayList<>();
            children.add(new FileNode("a.txt", 1));
            DirectoryNode directory = new DirectoryNode("docs", children);
            children.clear();
            equal(1, directory.getChildren().size());
            expect(UnsupportedOperationException.class, () -> directory.getChildren().clear());
        });
        run("file is a leaf and zero bytes is a valid size", () -> {
            FileNode file = new FileNode("empty.txt", 0);
            equal(0L, file.getSizeBytes());
            equal(List.of(), file.getChildren());
        });
        run("invalid names sizes and children are rejected", () -> {
            for (String name : new String[] {"", " ", "\t"}) {
                expect(IllegalArgumentException.class, () -> new FileNode(name, 1));
                expect(IllegalArgumentException.class, () -> new DirectoryNode(name, List.of()));
            }
            expect(NullPointerException.class, () -> new FileNode(null, 1));
            expect(NullPointerException.class, () -> new DirectoryNode(null, List.of()));
            expect(IllegalArgumentException.class, () -> new FileNode("a", -1));
            expect(NullPointerException.class, () -> new DirectoryNode("docs", null));
            List<FileSystemElement> children = new ArrayList<>();
            children.add(null);
            expect(NullPointerException.class, () -> new DirectoryNode("docs", children));
        });
        run("null visitors and traversal inputs are rejected", () -> {
            FileNode file = new FileNode("a", 1);
            DirectoryNode directory = new DirectoryNode("docs", List.of());
            expect(NullPointerException.class, () -> file.accept(null));
            expect(NullPointerException.class, () -> directory.accept(null));
            expect(NullPointerException.class, () -> new FileSystemWalker().walk(file, null));
            expect(NullPointerException.class,
                    () -> new FileSystemWalker().walk(null, new RecordingVisitor()));
        });
        System.out.println("PASS: " + passed + " tests");
    }

    private static void run(String name, CheckedTest test) throws Exception {
        test.run();
        passed++;
        System.out.println("PASS " + name);
    }

    private static void equal(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    private static void expect(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException error) {
            if (type.isInstance(error)) {
                return;
            }
            throw error;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    @FunctionalInterface
    private interface CheckedTest {
        void run() throws Exception;
    }

    private static final class RecordingVisitor implements FileSystemVisitor {
        private final List<String> events = new ArrayList<>();

        @Override
        public void visitFile(FileNode file) {
            events.add("file:" + file.getName());
        }

        @Override
        public void visitDirectory(DirectoryNode directory) {
            events.add("directory:" + directory.getName());
        }
    }
}
