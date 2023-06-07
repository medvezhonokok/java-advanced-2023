package info.kgeorgiy.ja.kim.walk;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

public final class WalkUtils {
    public static final String ERROR_CODE = "0".repeat(64);

    private WalkUtils() {
    }

    public static void writeHash(final Path file, final BufferedWriter bufferedWriter) {
        writeHash(file.toString(), bufferedWriter, getHash(file));
    }

    public static void writeHash(final String fileName, final BufferedWriter bufferedWriter, final String hash) {
        try {
            bufferedWriter.write(String.format("%s %s\n", hash, fileName));
        } catch (IOException e) {
            message("Cannot write to output file: " + fileName, e, e.getMessage());
        }
    }

    public static String getHash(final Path file) {
        final byte[] buffer = new byte[1 << 16];
        final MessageDigest DIGEST;

        try {
            DIGEST = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try (final InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
            // :NOTE: затирать буффер необязательно
            Arrays.fill(buffer, (byte) 0);
            for (int toRead = stream.read(buffer); toRead != -1; toRead = stream.read(buffer)) {
                DIGEST.update(buffer, 0, toRead);
            }
            return toHex(DIGEST.digest());
        } catch (IOException | InvalidPathException e) {
            message("Cannot calculate hash", e, e.getMessage());
            return ERROR_CODE;
        }
    }

    public static <T, R> void message(final String message, final R reason, final T info) {
        System.err.printf(
                """
                         Error: %s
                            Caused by: %s
                            More info: %s
                                     
                        """, message, reason.toString(), info.toString());
    }

    public static String toHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }
}
