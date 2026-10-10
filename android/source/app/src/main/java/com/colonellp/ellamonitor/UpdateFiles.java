package com.colonellp.ellamonitor;

import java.io.File;
import java.io.IOException;

/** File operations shared by downloading, cleanup and the read-only provider. */
final class UpdateFiles {
    static long contentLength(String header) {
        if (header == null) return -1L;
        try { long n = Long.parseLong(header.trim()); return n >= 0L ? n : -1L; }
        catch (NumberFormatException ignored) { return -1L; }
    }

    static void ensureDirectory(File directory) throws IOException {
        if (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory())
            throw new IOException("Cannot create update download directory");
    }

    static boolean delete(File file) {
        return !file.exists() || file.delete();
    }
}

