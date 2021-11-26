package ninja.egg82.mcpsearch.utils;

import ninja.egg82.mcpsearch.Main;
import ninja.egg82.mcpsearch.web.WebRequest;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    public static File getCurrentDirectory() throws URISyntaxException {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
    }

    public static File getVersionDirectory(String version) throws URISyntaxException {
        File cacheDirectory = new File(getCurrentDirectory(), "cache");
        return new File(cacheDirectory, version);
    }

    public static File getRevisionDirectory(String version, String revision, boolean isStable) throws URISyntaxException {
        File cacheDirectory = new File(getCurrentDirectory(), "cache");
        File versionDirectory = new File(cacheDirectory, version);
        File typeDirectory = new File(versionDirectory, (isStable) ? "stable" : "snapshot");
        return new File(typeDirectory, revision);
    }

    public static void extractFile(File zip, String fileName, File outputFile) throws IOException {
        try (FileInputStream stream = new FileInputStream(zip); BufferedInputStream bufferedIn = new BufferedInputStream(stream); ZipInputStream in = new ZipInputStream(bufferedIn); FileOutputStream out = new FileOutputStream(outputFile)) {
            ZipEntry ze;
            while ((ze = in.getNextEntry()) != null) {
                if (ze.getName().equals(fileName)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    break;
                }
            }
        }
    }

    public static void downloadFile(@NotNull String from, @NotNull File to) throws IOException {
        HttpURLConnection conn = WebRequest.builder(new URL(from))
                .timeout(new TimeUtil.Time(2500L, TimeUnit.MILLISECONDS))
                .userAgent("egg82/MCPSearch")
                .build()
                .getConnection();

        try (BufferedInputStream in = new BufferedInputStream(WebRequest.getInputStream(conn)); FileOutputStream out = new FileOutputStream(to)) {
            byte[] dataBuffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, dataBuffer.length)) > -1) {
                out.write(dataBuffer, 0, bytesRead);
            }
        }
    }

    public static @NotNull String readFileString(@NotNull File file) throws IOException {
        try (FileReader fileIn = new FileReader(file); BufferedReader in = new BufferedReader(fileIn)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
                builder.append(System.lineSeparator());
            }
            return builder.toString().trim();
        }
    }
}
