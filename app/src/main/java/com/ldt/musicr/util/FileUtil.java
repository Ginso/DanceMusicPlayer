package com.ldt.musicr.util;


import java.io.File;
import java.io.IOException;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public final class FileUtil {
    private FileUtil() {
    }

    public static String safeGetCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return file.getAbsolutePath();
        }
    }

}
