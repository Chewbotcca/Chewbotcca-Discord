package pw.chew.Chewbotcca.util;

import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.LoggerFactory;
import pw.chew.Chewbotcca.Main;

import java.io.IOException;

public class RestClient {
    public static String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        LoggerFactory.getLogger(RestClient.class).info("Making call to GET " + url);
        return performRequest(request);
    }

    public static String performRequest(Request request) {
        try (Response response = Main.jda.getHttpClient().newCall(request).execute()) {
            // System.out.println(r);
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
