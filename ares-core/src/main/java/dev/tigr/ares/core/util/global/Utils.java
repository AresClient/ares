package dev.tigr.ares.core.util.global;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Tigermouthbear
 */
public class Utils {
    public static double roundDouble(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static InputStream openURLStream(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/4.76");
        return connection.getInputStream();
    }

    public static double clamp(double num, double min, double max) {
        return num < min ? min : Math.min(num, max);
    }
}

