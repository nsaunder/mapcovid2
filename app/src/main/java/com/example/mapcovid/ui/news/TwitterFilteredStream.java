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

import com.github.redouane59.twitter.IAPIEventListener;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import com.github.redouane59.twitter.dto.stream.StreamRules.StreamMeta;
import com.github.redouane59.twitter.dto.stream.StreamRules.StreamRule;
import com.github.redouane59.twitter.dto.tweet.Tweet;
import com.github.redouane59.twitter.dto.tweet.TweetSearchResponse;
import com.github.redouane59.twitter.dto.user.FollowResponse;
import com.github.redouane59.twitter.dto.user.User;
import com.github.scribejava.core.model.Response;


public class TwitterFilteredStream extends Thread{
    public void run() {
        TwitterClient twitterClient = new TwitterClient(TwitterCredentials.builder()
                .accessToken("1576654423-ctIWZ6WsoIyNRPdLu5U3E4thUJ8l1Nvqi6OjpfM")
                .accessTokenSecret("G6OrLvAMIoK4MS4WRTo6P7cmYRbe3x8t8lYUx4wcW5xGy")
                .apiKey("2QqmHULXAdmAXmgmJCpANtpP0")
                .apiSecretKey("vN7afYDUkpQkTQSJh4LWFJgfykgM9yBrrWuMqWlMclxfGehTM8")
                .build());

        StreamMeta stream_meta = twitterClient.deleteFilteredStreamRule("covid");
        StreamRule covid_rule = twitterClient.addFilteredStreamRule("covid", "funny things");
        Future<Response> future_response = twitterClient.startFilteredStream(new IAPIEventListener() {
            @Override
            public void onStreamError(int httpCode, String error) {
                System.out.println(httpCode);
                System.out.println(error);
            }

            @Override
            public void onTweetStreamed(Tweet tweet) {
                System.out.println(tweet.getText());
            }

            @Override
            public void onUnknownDataStreamed(String json) {
                System.out.println(json);
            }

            @Override
            public void onStreamEnded(Exception e) {
                System.out.println((e.toString()));
            }
        });
        //twitterClient.stopFilteredStream(future_response);
    }
}