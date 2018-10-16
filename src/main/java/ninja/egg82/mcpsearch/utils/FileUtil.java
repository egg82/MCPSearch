package ninja.egg82.mcpsearch.utils;

import ninja.egg82.mcpsearch.Main;

import java.io.File;
import java.net.URISyntaxException;

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
}
