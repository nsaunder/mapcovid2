package com.example.mapcovid.ui.news;
import android.os.AsyncTask;

import com.example.mapcovid.R;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.config.CookieSpecs;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.DefaultHttpRequestRetryHandler;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.client.HttpClient;

import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.dto.stream.StreamRules.StreamMeta;
import com.github.redouane59.twitter.dto.stream.StreamRules.StreamRule;
import com.github.redouane59.twitter.dto.tweet.Tweet;
import com.github.redouane59.twitter.dto.tweet.TweetSearchResponse;
import com.github.redouane59.twitter.dto.user.FollowResponse;
import com.github.redouane59.twitter.dto.user.User;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import com.github.scribejava.core.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TwitterFilteredStream extends Thread {
    public void run() {
        String bearerToken = "AAAAAAAAAAAAAAAAAAAAAADOGAEAAAAAFjb0h4FR3Tx0pq0LIxAdR1ycabA%3DwsY4twYrLfRSc8o9MkBikSxl3tSETn7NddIR1hCoEMaqbI94ot";
        if (null != bearerToken) {
            Map<String, String> rules = new HashMap<>();
            rules.put("covid lang:en", "covidrule");
            try {
                setupRules(bearerToken, rules);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            try {
                connectStream(bearerToken);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("There was a problem getting you bearer token. Please make sure you set the BEARER_TOKEN environment variable");
        }

    }
    /*
     * This method calls the filtered stream endpoint and streams Tweets from it
     * */
    private static void connectStream(String bearerToken) throws IOException, URISyntaxException {
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(30000).setConnectTimeout(5000).setCookieSpec(CookieSpecs.STANDARD).build())
                .setRetryHandler(new DefaultHttpRequestRetryHandler(5, true))
                .build();

        URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/search/stream");

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();

        try {
            if (null != entity) {
                BufferedReader reader = new BufferedReader(new InputStreamReader((entity.getContent())));
                String line = reader.readLine();
                int counter = 0;
                while (line != null && counter < 50) {
                    System.out.println(line);
                    line = reader.readLine();
                    counter += 1;
                }
                httpGet.releaseConnection();
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

    }

    /*
     * Helper method to setup rules before streaming data
     * */
    private static void setupRules(String bearerToken, Map<String, String> rules) throws IOException, URISyntaxException {
        List<String> existingRules = getRules(bearerToken);
        if (existingRules.size() > 0) {
            deleteRules(bearerToken, existingRules);
        } else {
            createRules(bearerToken, rules);
        }
    }

    /*
     * Helper method to create rules for filtering
     * */
    private static void createRules(String bearerToken, Map<String, String> rules) throws URISyntaxException, IOException {
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/search/stream/rules");

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpPost.setHeader("content-type", "application/json");
        StringEntity body = new StringEntity(getFormattedString("{\"add\": [%s]}", rules));
        httpPost.setEntity(body);
        HttpResponse response = httpClient.execute(httpPost);
        System.out.println("FLAG");
        System.out.println(response);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            System.out.println(EntityUtils.toString(entity, "UTF-8"));
        }
    }

    /*
     * Helper method to get existing rules
     * */
    private static List<String> getRules(String bearerToken) throws URISyntaxException, IOException {
        List<String> rules = new ArrayList<>();
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/search/stream/rules");

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpGet.setHeader("content-type", "application/json");
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            try {
                JSONObject json = new JSONObject(EntityUtils.toString(entity, "UTF-8"));
                if (json.length() > 1) {
                    JSONArray array = (JSONArray) json.get("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = (JSONObject) array.get(i);
                        rules.add(jsonObject.getString("id"));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return rules;
    }

    /*
     * Helper method to delete rules
     * */
    private static void deleteRules(String bearerToken, List<String> existingRules) throws URISyntaxException, IOException {
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/search/stream/rules");

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpPost.setHeader("content-type", "application/json");
        StringEntity body = new StringEntity(getFormattedString("{ \"delete\": { \"ids\": [%s]}}", existingRules));
        httpPost.setEntity(body);
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            System.out.println(EntityUtils.toString(entity, "UTF-8"));
        }
    }

    private static String getFormattedString(String string, List<String> ids) {
        StringBuilder sb = new StringBuilder();
        if (ids.size() == 1) {
            return String.format(string, "\"" + ids.get(0) + "\"");
        } else {
            for (String id : ids) {
                sb.append("\"" + id + "\"" + ",");
            }
            String result = sb.toString();
            return String.format(string, result.substring(0, result.length() - 1));
        }
    }

    private static String getFormattedString(String string, Map<String, String> rules) {
        StringBuilder sb = new StringBuilder();
        if (rules.size() == 1) {
            String key = rules.keySet().iterator().next();
            return String.format(string, "{\"value\": \"" + key + "\", \"tag\": \"" + rules.get(key) + "\"}");
        } else {
            for (Map.Entry<String, String> entry : rules.entrySet()) {
                String value = entry.getKey();
                String tag = entry.getValue();
                sb.append("{\"value\": \"" + value + "\", \"tag\": \"" + tag + "\"}" + ",");
            }
            String result = sb.toString();
            return String.format(string, result.substring(0, result.length() - 1));
        }
    }

}
