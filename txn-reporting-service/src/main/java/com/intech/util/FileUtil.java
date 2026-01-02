package com.intech.util;

import java.io.IOException;
import java.nio.file.*;

/**
 * File system helpers used for safe XML generation.
 *
 * Production rules:
 * - write to a temp file first
 * - atomically move to final file name
 */
public final class FileUtil {

    private FileUtil() { }

    public static Path tempFile(Path outputDir, String baseFileName) throws IOException {
        String tmpName = baseFileName + ".tmp";
        return outputDir.resolve(tmpName);
    }

    public static Path finalFile(Path outputDir, String baseFileName) {
        return outputDir.resolve(baseFileName);
    }

    public static void atomicMove(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // Some filesystems do not support atomic move; fallback to non-atomic replace.
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
