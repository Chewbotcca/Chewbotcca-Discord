/*
 * Copyright (C) 2020 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pw.chew.chewbotcca.util;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.Main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Off brand RestClient based on the ruby gem of the same name
public class RestClient {
    /**
     * Make a GET request
     * @param url the url to get
     * @return a response
     */
    public static String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Chewbotcca-5331/1.0 (JDA; +https://chew.pw/chewbotcca) DBots/604362556668248095")
                .build();

        LoggerFactory.getLogger(RestClient.class).debug("Making call to GET " + url);
        return performRequest(request);
    }

    /**
     * Make an Authenticated GET Request
     * @param url the url
     * @param key the auth key
     * @return a response
     */
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

    /**
     * Make an Authenticated POST Request
     * @param url the url
     * @param args the arguments to pass
     * @param key the auth key
     * @return a response
     */
    public static String post(String url, HashMap<String, Object> args, String key) {
        Request request = new Request.Builder()
                .url(url)
                .post(bodyFromHash(args))
                .addHeader("Authorization", key)
                .addHeader("User-Agent", "Chewbotcca-5331/1.0 (JDA; +https://chew.pw/chewbotcca) DBots/604362556668248095")
                .build();

        return performRequest(request);
    }

    /**
     * Actually perform the request
     * @param request a request
     * @return a response
     */
    public static String performRequest(Request request) {
        try (Response response = Main.getJDA().getHttpClient().newCall(request).execute()) {
            String body;
            if(response.body() == null) {
                body = null;
            } else {
                body = response.body().string();
            }
            LoggerFactory.getLogger(RestClient.class).debug("Response is " + body);
            return body;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RequestBody bodyFromHash(HashMap<String, Object> args) {
        FormBody.Builder bodyArgs = new FormBody.Builder();
        for(Map.Entry<String, Object> entry : args.entrySet()) {
            bodyArgs.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return bodyArgs.build();
    }
}
