package ninja.egg82.mcpsearch.utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {
    private ZipUtil() {}

    public static void extractFile(File zip, String fileName, File outputFile) throws IOException {
        try (FileInputStream stream = new FileInputStream(zip); BufferedInputStream bufferedIn = new BufferedInputStream(stream); ZipInputStream in = new ZipInputStream(bufferedIn); FileOutputStream out = new FileOutputStream(outputFile)) {
            ZipEntry ze;
            while ((ze = in.getNextEntry()) != null) {
                if (ze.getName().equals(fileName)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    break;
                }
            }
        }
    }
}
