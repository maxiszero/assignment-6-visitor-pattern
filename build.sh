#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
task="${1:-demo}"
case "$task" in
  demo|extension|test|compile) ;;
  *) echo 'Usage: sh build.sh [demo|extension|test|compile]' >&2; exit 2 ;;
esac
mkdir -p build/classes
find src/main/java src/test/java -name '*.java' | sort > build/sources.txt
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build/classes @build/sources.txt
case "$task" in
  test) java -cp build/classes filesystem.VisitorTest ;;
  extension) java -cp build/classes filesystem.ExtensionDemo ;;
  demo) java -cp build/classes filesystem.Main ;;
  compile) echo 'Compilation successful. Classes are in build/classes.' ;;
esac
