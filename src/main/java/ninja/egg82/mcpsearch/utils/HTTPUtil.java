package ninja.egg82.mcpsearch.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPUtil {
    private HTTPUtil() {}

    public static void downloadFile(URL url, File output) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(getInputStream(url)); FileOutputStream fileOutputStream = new FileOutputStream(output)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
    }

    public static HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(true);

        int status;
        boolean redirect;

        do {
            status = conn.getResponseCode();
            redirect = status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER;

            if (redirect) {
                String newUrl = conn.getHeaderField("Location");
                String cookies = conn.getHeaderField("Set-Cookie");

                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                conn.setRequestProperty("Cookie", cookies);
                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            }
        } while (redirect);

        return conn;
    }

    public static InputStream getInputStream(URL url) throws IOException {
        HttpURLConnection conn = getConnection(url);
        int status = conn.getResponseCode();

        if (status >= 400 && status < 600) {
            // 400-500 errors
            throw new IOException("Server returned status code " + status);
        }

        return conn.getInputStream();
    }
}
