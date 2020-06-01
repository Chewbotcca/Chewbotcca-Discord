package pw.chew.Chewbotcca.util;

import okhttp3.Request;
import okhttp3.Response;
import pw.chew.Chewbotcca.Main;

import java.io.IOException;

public class RestClient {
    public static String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return performRequest(request);
    }

    public static String performRequest(Request request) {
        try (Response response = Main.jda.getHttpClient().newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
