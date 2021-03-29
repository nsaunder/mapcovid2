package com.example.mapcovid;

import com.github.redouane59.twitter.dto.tweet.Tweet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TweetTest {
    NewsFragment nf = new NewsFragment();
    List<Tweet> tweets;

    public synchronized List<Tweet> gettweets() throws InterruptedException {
        List<Tweet> temp = nf.helperTweets();
        return temp;
    }

    @Test
    public void loads() throws InterruptedException {
        tweets = gettweets();
        int len = tweets.size();
        assertNotEquals(0, len);
        assertEquals(20, len);
    }
}