package pw.chew.chewbotcca.util;

import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.Main;

import java.io.IOException;

public class RestClient {
    public static String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Chewbotcca-5331/1.0 (JDA; +https://chew.pw/chewbotcca) DBots/604362556668248095")
                .build();

        LoggerFactory.getLogger(RestClient.class).debug("Making call to GET " + url);
        return performRequest(request);
    }

    public static String get(String url, String key) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", key)
                .addHeader("User-Agent", "Chewbotcca-5331/1.0 (JDA; +https://chew.pw/chewbotcca) DBots/604362556668248095")
                .get()
                .build();

        LoggerFactory.getLogger(RestClient.class).debug("Making call to GET " + url);
        return performRequest(request);
    }

    public static String performRequest(Request request) {
        try (Response response = Main.jda.getHttpClient().newCall(request).execute()) {
            // System.out.println(r);
            String ye = response.body().string();
            LoggerFactory.getLogger(RestClient.class).debug("Response is " + ye);
            return ye;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
