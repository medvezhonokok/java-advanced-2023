package info.kgeorgiy.ja.kim.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;

public class WalkService {

    protected static boolean invalidArguments(String[] args) {
        if (args == null) {
            WalkUtils.message("Arguments are null", "args = 'null'", "");
            return true;
        }

        if (args.length != 2) {
            WalkUtils.message("Invalid arguments length", "excepted: 2, found: " + args.length, "");
            return true;
        }

        if (args[1] == null || args[0] == null) {
            WalkUtils.message("Invalid arguments", "args = " + Arrays.toString(args), "");
            return true;
        }
        return false;
    }

    public static void walk(final String nameOfInputFile, final String nameOfOutputFile, boolean recursiveWalk) {
        try {
            final Path inputFile = Path.of(nameOfInputFile);
            final Path outputFile = Path.of(nameOfOutputFile);
            final Path parent = outputFile.getParent();

            if (parent != null) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    WalkUtils.message("Cannot create directories", "child directory is null, or missing.", e);
                }
            }

            try (final BufferedReader bufferedReader = Files.newBufferedReader(inputFile)) {
                try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile)) {
                    String nameOfNextFile;
                    while ((nameOfNextFile = bufferedReader.readLine()) != null) {
                        handle(nameOfNextFile, bufferedWriter, recursiveWalk);
                    }
                } catch (IOException e) {
                    WalkUtils.message(
                            "Cannot open output file: " + outputFile,
                            "cannot read from input file" + e, e.getMessage()
                    );
                }
            } catch (IOException e) {
                WalkUtils.message("Cannot open input file: " + inputFile, e, e.getMessage());
            }
        } catch (InvalidPathException e) {
            WalkUtils.message("Invalid path: " + nameOfInputFile, e, e.getMessage());
        }
    }

    private static void handle(final String nameOfNextFile, final BufferedWriter bufferedWriter,
                               boolean recursiveWalk) {
        Path fileName = null;

        try {
            fileName = Path.of(nameOfNextFile);
        } catch (InvalidPathException e) {
            WalkUtils.message("Invalid file path " + nameOfNextFile, e, e.getMessage());
        }

        try {
            if (fileName != null) {
                if (recursiveWalk) {
                    Files.walkFileTree(fileName, new Sha256FileVisitor(bufferedWriter));
                } else {
                    WalkUtils.writeHash(fileName, bufferedWriter);
                }
            } else {
                WalkUtils.writeHash(nameOfNextFile, bufferedWriter, WalkUtils.ERROR_CODE);
            }
        } catch (InvalidPathException | IOException e) {
            WalkUtils.message("Cannot walk by file tree: " + e.getMessage(), e, "hashed it with 64 zeroes.");
            WalkUtils.writeHash(nameOfNextFile, bufferedWriter, WalkUtils.ERROR_CODE);
        }
    }
}
