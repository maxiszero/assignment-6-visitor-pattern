package filesystem;

public final class ExtensionDemo {
    private ExtensionDemo() { }

    public static void main(String[] args) {
        FileSystemElement root = SampleFileSystem.create();
        ElementCountVisitor counts = new ElementCountVisitor();
        new FileSystemWalker().walk(root, counts);

        System.out.println("New operation on the same sample tree: " + root.getName());
        System.out.println("File count: " + counts.getFileCount());
        System.out.println("Directory count (including root): " + counts.getDirectoryCount());
    }
}
