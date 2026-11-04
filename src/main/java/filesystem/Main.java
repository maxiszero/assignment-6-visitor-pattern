package filesystem;

/** Client: applies two unrelated operations to the same immutable object structure. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        FileSystemElement root = SampleFileSystem.create();
        FileSystemWalker walker = new FileSystemWalker();

        SizeCalculatorVisitor size = new SizeCalculatorVisitor();
        walker.walk(root, size);
        System.out.println("Same file system: project (4 files, 3 directories)");
        System.out.println("Total file-content size: " + size.getTotalBytes() + " bytes");

        SearchVisitor search = new SearchVisitor(".txt");
        walker.walk(root, search);
        System.out.println("Names containing .txt:");
        for (FileSystemElement match : search.getMatches()) {
            System.out.println("- " + match.getName());
        }

        SearchVisitor directories = new SearchVisitor("docs");
        walker.walk(root, directories);
        System.out.println("Names containing docs:");
        for (FileSystemElement match : directories.getMatches()) {
            System.out.println("- " + match.getName());
        }
        System.out.println("All visitors used the same root; no element was changed.");
    }
}
