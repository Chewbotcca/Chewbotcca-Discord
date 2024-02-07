/*
 * Copyright (C) 2024 Chewbotcca
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Off brand RestClient based on the ruby gem of the same name
 */
public class RestClient {
    public static final String JSON = "application/json; charset=utf-8";
    private static String userAgent = "Java Discord Bot";
    private static final HttpClient client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    private static Duration timeout = Duration.ofSeconds(30);
    private static boolean debug = true;

    /**
     * Gets the user agent used for this session.
     *
     * @return The user agent.
     */
    public static String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the user agent used for this session.
     *
     * @param userAgent The new user agent.
     */
    public static void setUserAgent(String userAgent) {
        RestClient.userAgent = userAgent;
    }

    /**
     * Gets the HTTP client used for this session
     *
     * @return The HTTP Client
     */
    public static HttpClient getHttpClient() {
        return client;
    }

    /**
     * Sets the timeout used for this session.
     *
     * @param timeout The new timeout.
     */
    public static void setTimeout(Duration timeout) {
        RestClient.timeout = timeout;
    }

    /**
     * Sets debug logging for this session.
     *
     * @param debug The new debug option.
     */
    public static void setDebug(boolean debug) {
        RestClient.debug = debug;
    }

    /**
     * Make a GET request
     *
     * @param url the url to get
     * @param headers Optional set of headers as "Header: Value" like "Authorization: Bearer bob"
     * @throws IllegalArgumentException If an invalid header is passed
     * @throws RuntimeException If the request fails
     * @return a String response
     */
    public static Response get(String url, String ...headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
            .header("User-Agent", userAgent)
            .timeout(timeout);

        for (String header : headers) {
            String[] details = header.split(":");
            if (details.length != 2) {
                throw new IllegalArgumentException("Invalid header syntax provided: " + header);
            }
            request.header(details[0].trim(), details[1].trim());
        }

        if (debug) LoggerFactory.getLogger(RestClient.class).debug("Making call to GET " + url);
        return performRequest(request.build());
    }

    /**
     * Make a POST Request
     *
     * @param url the url
     * @param data the body to send (will run through toString())
     * @param headers vararg of headers in "Key: Value" format
     * @throws IllegalArgumentException If an invalid header is passed
     * @throws RuntimeException If the request fails
     * @return a response
     */
    public static Response post(String url, Object data, String... headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
            .header("User-Agent", userAgent)
            .timeout(timeout);

        for (String header : headers) {
            String[] details = header.split(":");
            if (details.length != 2) {
                throw new IllegalArgumentException("Invalid header syntax provided: " + header);
            }
            request.header(details[0].trim(), details[1].trim());
        }

        if (data instanceof JSONObject || data instanceof JSONArray) {
            request.header("Content-Type", JSON);
        }

        if (debug) LoggerFactory.getLogger(RestClient.class).debug("Making call to POST {}", url);
        return performRequest(request.build());
    }

    /**
     * Actually perform the request
     * @param request a request
     * @return a response
     */
    public static Response performRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();
            if (debug) {
                LoggerFactory.getLogger(RestClient.class).debug("Response is " + body);
            }
            return new Response(code, body);
        } catch (IOException | InterruptedException e) {
            // Rethrow exceptions as runtime
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * A response from a REST call
     */
    public record Response(int code, String response) {
        /**
         * Check to see if the request was successful.
         * Codes 200-299 are considered successful.
         * @return true if successful
         */
        public boolean success() {
            return code >= 200 && code < 300;
        }

        /**
         * Get the response as a String
         * @return a String
         */
        public String asString() {
            return response;
        }

        /**
         * Get the response as a JSONObject
         * @return a JSONObject
         */
        public JSONObject asJSONObject() {
            return new JSONObject(response);
        }

        /**
         * Get the response as a JSONArray
         * @return a JSONArray
         */
        public JSONArray asJSONArray() {
            return new JSONArray(response);
        }

        /**
         * Get the response as a String
         * @return a String
         */
        @Override
        public String toString() {
            return asString();
        }
    }
}
