package pl.pawelkielb.fchat.client.exceptions;

import java.nio.file.Path;

public class FileWriteException extends RuntimeException {
    private final Path path;

    public FileWriteException(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}