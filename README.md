# Assignment 6 · Visitor Pattern

**Option A: File System Visitor** · Java 17 · No external dependencies

The application builds an in-memory tree of files and directories. The same tree is passed to a size visitor and a name search visitor. A separate entry point demonstrates a third operation, counting files and directories, added without modifying the element classes or their interfaces.

## Run

Install JDK 17 or newer and make sure `java` and `javac` are on PATH. Open a terminal in the project folder.

| Task | Windows PowerShell | Linux/macOS |
| --- | --- | --- |
| Size and name search | `powershell -ExecutionPolicy Bypass -File .\build.ps1 demo` | `sh build.sh demo` |
| Third visitor | `powershell -ExecutionPolicy Bypass -File .\build.ps1 extension` | `sh build.sh extension` |
| Tests | `powershell -ExecutionPolicy Bypass -File .\build.ps1 test` | `sh build.sh test` |
| Compile only | `powershell -ExecutionPolicy Bypass -File .\build.ps1 compile` | `sh build.sh compile` |

The main demo reports **6000 bytes**, finds `README.txt`, `notes.txt`, and `guide.txt` with `.txt`, and finds the directory `docs` with `docs`. The extension reports **4 files and 3 directories**, including the root. The test runner reports `PASS: 20 tests`.

## Where to look

- [Main.java](src/main/java/filesystem/Main.java) creates one tree and applies both operations.
- [FileSystemWalker.java](src/main/java/filesystem/FileSystemWalker.java) visits the root and then its children in order, calling `accept` once per entry.
- [FileNode.java](src/main/java/filesystem/FileNode.java) calls `visitFile(this)`; [DirectoryNode.java](src/main/java/filesystem/DirectoryNode.java) calls `visitDirectory(this)`. The visitor implementation supplies the operation. The walker needs no element type checks.
- [ElementCountVisitor.java](src/main/java/filesystem/ElementCountVisitor.java) and [ExtensionDemo.java](src/main/java/filesystem/ExtensionDemo.java) show how to add an operation.
- [Report (PDF)](docs/Assignment_6_Visitor_Pattern_Report.pdf) · [UML diagram](docs/class-diagram.svg) · [PlantUML source](docs/visitor.puml) · [Defense guide (Russian)](docs/DEFENSE_RU.md)
