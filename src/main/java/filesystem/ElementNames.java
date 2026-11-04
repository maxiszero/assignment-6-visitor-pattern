package filesystem;

import java.util.Objects;

/** Shared name validation keeps file and directory rules consistent. */
final class ElementNames {
    private ElementNames() { }

    static String requireValid(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Element name must not be blank");
        }
        return name;
    }
}
