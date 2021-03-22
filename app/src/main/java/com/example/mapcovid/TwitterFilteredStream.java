package com.example.mapcovid;

import com.github.redouane59.twitter.IAPIEventListener;
import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.dto.stream.StreamRules.StreamMeta;
import com.github.redouane59.twitter.dto.stream.StreamRules.StreamRule;
import com.github.redouane59.twitter.dto.tweet.Tweet;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import com.github.scribejava.core.model.Response;

import java.util.*;

import java.util.concurrent.Future;

interface TweetStream {
    void onCallBack(ArrayList<Tweet> tweets);
}

interface TweetListener {
    void onListener();
}

public class TwitterFilteredStream extends Thread {
    private ArrayList<Tweet> tweets;
    private Future<Response> future_response;
    private boolean twentyreached = false;
    List<TweetListener> tweetlisteners = new ArrayList<TweetListener>();

    public void addListeners(TweetListener tl) {
        tweetlisteners.add(tl);
    }

    public void setVar() {
        twentyreached = true;
        for (TweetListener tl: tweetlisteners) {
            tl.onListener();
        }
    }

    public void run() {
        TwitterClient twitterClient = new TwitterClient(TwitterCredentials.builder()
                .accessToken("1576654423-ctIWZ6WsoIyNRPdLu5U3E4thUJ8l1Nvqi6OjpfM")
                .accessTokenSecret("G6OrLvAMIoK4MS4WRTo6P7cmYRbe3x8t8lYUx4wcW5xGy")
                .apiKey("2QqmHULXAdmAXmgmJCpANtpP0")
                .apiSecretKey("vN7afYDUkpQkTQSJh4LWFJgfykgM9yBrrWuMqWlMclxfGehTM8")
                .build());

        tweets = new ArrayList<Tweet>();

        StreamMeta stream_meta = twitterClient.deleteFilteredStreamRule("covid OR coronavirus OR covid-19 lang:en is:verified");

        StreamRule s_r1 = twitterClient.addFilteredStreamRule("covid OR coronavirus OR covid-19 lang:en is:verified", "language1");

        startStream(twitterClient, new TweetStream() {
            @Override
            public void onCallBack(ArrayList<Tweet> tweets) {
                setVar();
                twitterClient.stopFilteredStream(future_response);
            }
        });

        System.out.println("FLAG " + tweets.size());
        if (tweets.size() == 20) {
            System.out.println("REACHED STOPPED");
        }
    }

    public void startStream(TwitterClient twitterClient, final TweetStream tweetStream) {
        future_response = twitterClient.startFilteredStream(new IAPIEventListener() {
            int i = 0;
            @Override
            public void onStreamError(int httpCode, String error) {
                System.out.println(httpCode);
                System.out.println(error);
            }

            @Override
            public void onTweetStreamed(Tweet tweet) {
                if (i < 20) {
                    tweets.add(tweet);
                    i++;
                } else {
                    tweetStream.onCallBack(tweets);
                }
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
    }
    public ArrayList<Tweet> getTweets() {
        return tweets;
    }
}