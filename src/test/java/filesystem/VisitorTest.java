package filesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        run("size visitor sums nested files without counting directories twice", () -> {
            SizeCalculatorVisitor visitor = new SizeCalculatorVisitor();
            new FileSystemWalker().walk(sampleTree(), visitor);
            equal(6000L, visitor.getTotalBytes());
        });
        run("an empty directory has zero content bytes", () -> {
            SizeCalculatorVisitor visitor = new SizeCalculatorVisitor();
            new FileSystemWalker().walk(new DirectoryNode("empty", List.of()), visitor);
            equal(0L, visitor.getTotalBytes());
        });
        run("file sizes larger than int remain correct", () -> {
            SizeCalculatorVisitor visitor = new SizeCalculatorVisitor();
            new FileNode("large.bin", 3_000_000_000L).accept(visitor);
            equal(3_000_000_000L, visitor.getTotalBytes());
        });
        run("size overflow is reported instead of returning a negative total", () -> {
            SizeCalculatorVisitor visitor = new SizeCalculatorVisitor();
            FileSystemElement root = new DirectoryNode("large", List.of(
                    new FileNode("a", Long.MAX_VALUE), new FileNode("b", 1)));
            expect(ArithmeticException.class, () -> new FileSystemWalker().walk(root, visitor));
        });
        run("search finds matching files in traversal order", () -> {
            SearchVisitor visitor = new SearchVisitor(".txt");
            new FileSystemWalker().walk(sampleTree(), visitor);
            equal(List.of("README.txt", "notes.txt", "guide.txt"), names(visitor.getMatches()));
        });
        run("search also handles directory elements", () -> {
            SearchVisitor visitor = new SearchVisitor("docs");
            new FileSystemWalker().walk(sampleTree(), visitor);
            equal(List.of("docs"), names(visitor.getMatches()));
        });
        run("case-insensitive search is independent of the machine locale", () -> {
            Locale original = Locale.getDefault();
            try {
                Locale.setDefault(Locale.forLanguageTag("tr-TR"));
                SearchVisitor visitor = new SearchVisitor("FILE");
                new FileNode("file.txt", 1).accept(visitor);
                equal(List.of("file.txt"), names(visitor.getMatches()));
            } finally {
                Locale.setDefault(original);
            }
        });
        run("search returns an empty list when nothing matches", () -> {
            SearchVisitor visitor = new SearchVisitor("missing");
            new FileSystemWalker().walk(sampleTree(), visitor);
            equal(List.of(), visitor.getMatches());
        });
        run("search rejects null and blank criteria", () -> {
            expect(NullPointerException.class, () -> new SearchVisitor(null));
            expect(IllegalArgumentException.class, () -> new SearchVisitor("  "));
        });
        run("search results are immutable snapshots", () -> {
            SearchVisitor visitor = new SearchVisitor(".txt");
            new FileNode("a.txt", 1).accept(visitor);
            List<FileSystemElement> snapshot = visitor.getMatches();
            expect(UnsupportedOperationException.class, snapshot::clear);
            new FileNode("b.txt", 1).accept(visitor);
            equal(List.of("a.txt"), names(snapshot));
            equal(List.of("a.txt", "b.txt"), names(visitor.getMatches()));
        });
        run("fresh visitors isolate repeated operations on the same tree", () -> {
            FileSystemElement root = sampleTree();
            FileSystemWalker walker = new FileSystemWalker();
            SizeCalculatorVisitor first = new SizeCalculatorVisitor();
            SearchVisitor search = new SearchVisitor(".txt");
            SizeCalculatorVisitor second = new SizeCalculatorVisitor();
            walker.walk(root, first);
            walker.walk(root, search);
            walker.walk(root, second);
            equal(6000L, first.getTotalBytes());
            equal(6000L, second.getTotalBytes());
            equal(3, search.getMatches().size());
            equal("project", root.getName());
        });
        run("parallel traversals share immutable nodes but use independent visitors", () -> {
            FileSystemElement root = sampleTree();
            FileSystemWalker walker = new FileSystemWalker();
            var executor = Executors.newFixedThreadPool(2);
            try {
                var size = executor.submit(() -> {
                    SizeCalculatorVisitor visitor = new SizeCalculatorVisitor();
                    walker.walk(root, visitor);
                    return visitor.getTotalBytes();
                });
                var matches = executor.submit(() -> {
                    SearchVisitor visitor = new SearchVisitor(".txt");
                    walker.walk(root, visitor);
                    return names(visitor.getMatches());
                });
                equal(6000L, size.get(5, TimeUnit.SECONDS));
                equal(List.of("README.txt", "notes.txt", "guide.txt"),
                        matches.get(5, TimeUnit.SECONDS));
            } finally {
                executor.shutdownNow();
            }
        });
        run("the new visitor counts files and directories separately", () -> {
            ElementCountVisitor visitor = new ElementCountVisitor();
            new FileSystemWalker().walk(SampleFileSystem.create(), visitor);
            equal(4, visitor.getFileCount());
            equal(3, visitor.getDirectoryCount());
        });
        run("the root is counted even when a directory is empty", () -> {
            ElementCountVisitor visitor = new ElementCountVisitor();
            new FileSystemWalker().walk(new DirectoryNode("empty", List.of()), visitor);
            equal(0, visitor.getFileCount());
            equal(1, visitor.getDirectoryCount());
        });
        System.out.println("PASS: " + passed + " tests");
    }

    private static void run(String name, CheckedTest test) throws Exception {
        test.run();
        passed++;
        System.out.println("PASS " + name);
    }

    private static FileSystemElement sampleTree() {
        return new DirectoryNode("project", List.of(
                new FileNode("README.txt", 1200),
                new DirectoryNode("src", List.of(
                        new FileNode("Main.java", 2400), new FileNode("notes.txt", 600))),
                new DirectoryNode("docs", List.of(new FileNode("guide.txt", 1800)))));
    }

    private static List<String> names(List<FileSystemElement> elements) {
        return elements.stream().map(FileSystemElement::getName).toList();
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
