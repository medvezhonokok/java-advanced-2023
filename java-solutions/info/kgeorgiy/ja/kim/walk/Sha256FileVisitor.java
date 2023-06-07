package info.kgeorgiy.ja.kim.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Sha256FileVisitor extends SimpleFileVisitor<Path> {
    private final BufferedWriter bufferedWriter;

    public Sha256FileVisitor(final BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes basicFileAttributes) {
        WalkUtils.writeHash(file, bufferedWriter);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
        WalkUtils.writeHash(file.toString(), bufferedWriter, WalkUtils.ERROR_CODE);
        return FileVisitResult.TERMINATE;
    }
}

