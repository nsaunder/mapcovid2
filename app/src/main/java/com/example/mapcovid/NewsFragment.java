package com.example.mapcovid;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.example.mapcovid.ui.news.NewsViewModel;
import com.github.redouane59.twitter.dto.tweet.Tweet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {
    private NewsViewModel mViewModel;
    public static NewsFragment newInstance() {
        return new NewsFragment();
    }

    public synchronized List<Tweet> helperTweets() throws InterruptedException {
        TwitterFilteredStream t1 = new TwitterFilteredStream();
        t1.start();
        List<Tweet> return_tweets = new ArrayList<Tweet>();
        t1.addListeners(new TweetListener() {
            @Override
            public void onListener() {
                List<Tweet> temp = t1.getTweets();
                for (Tweet tweet: temp) {
                    return_tweets.add(tweet);
                }
            }
        });
        wait(2000);
        return return_tweets;
    }

    public void printWeather(String weather, View view){
        try {
            String[] pieces = weather.split("_");
            for (String s : pieces) {
                System.out.println(s);
            }
            TextView wet = (TextView) view.findViewById(R.id.weather_text);
            wet.setText("Weather in Los Angeles: " + pieces[1] + "°, " + pieces[0] + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);

        try{
        ArrayList<Tweet> tweets_mainview = new ArrayList<Tweet>();
        TwitterFilteredStream t1 = new TwitterFilteredStream();
        t1.start();

        t1.addListeners(new TweetListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onListener() {
                ScrollView sv = (ScrollView) getView().findViewById(R.id.tweet_scroll_view);
                LinearLayout ll = (LinearLayout) getView().findViewById(R.id.tweet_linear_layout);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
//                params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                params.setMargins(10,10,10,10);
                for (Tweet tweet : t1.getTweets()) {
                    TextView temp = new TextView(getContext());
                    LinearLayout tweetLayout = new LinearLayout(getContext());
                    temp.post(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void run() {
//                            tweetLayout.setLayoutParams(params);
//                            TextView username = new TextView(getContext());
//                            TextView body = new TextView(getContext());
//                            TextView date = new TextView(getContext());
//                            username.setText("@" + tweet.getUser().getDisplayedName());
//                            body.setText(tweet.getText());
//                            //Somehow insert _______
//                            date.setText(tweet.getCreatedAt().getDayOfMonth()+"/"+tweet.getCreatedAt().getMonth()+"/"+tweet.getCreatedAt().getYear());
//
//                            username.setBackgroundColor(Color.GRAY);
//                            date.setBackgroundColor(Color.BLUE);
//                            tweetLayout.setBackgroundResource(R.drawable.back);
//                            tweetLayout.addView(username);
//                            tweetLayout.addView(body);
//                            tweetLayout.addView(date);
//                            System.out.println(tweet.getCreatedAt().getDayOfMonth()+"/"+tweet.getCreatedAt().getMonth()+"/"+tweet.getCreatedAt().getYear());
                            temp.setPadding(30,30,30,30);
                            temp.setText("@LACovid19Bot"   + "\n\n" + tweet.getText() + "\n\n" + tweet.getCreatedAt().getMonth() + " " + tweet.getCreatedAt().getDayOfMonth()+", "+tweet.getCreatedAt().getYear());
                            temp.setBackgroundColor(Color.GRAY);
                            temp.setBackgroundResource(R.drawable.back);
                            temp.setLayoutParams(params);
                        }
                    });
                    ll.post(new Runnable() {
                        @Override
                        public void run() {
                            ll.addView(temp);
                            // returnabletweets.add(tweet);
                        }
                    });
                }
            }
        });}
        catch (Exception e){
            LinearLayout ll = (LinearLayout) getView().findViewById(R.id.tweet_linear_layout);
            TextView temp = new TextView(getContext());
            temp.setText("Turn on wifi please :(");
            ll.addView(temp);
        }
        Python python = Python.getInstance();
        PyObject pythonFile = python.getModule("test");

        try {
            PyObject weatherString = pythonFile.callAttr("get_weather");
            printWeather(weatherString.toString(), view);
        }
        catch (Exception e){
            System.out.println(":/");
        }
        return view;
    }

//    public void setText(ArrayList<Tweet> tweets, View view) {
//        ScrollView sv = (ScrollView) view.findViewById(R.id.tweet_scroll_view);
//        LinearLayout ll = (LinearLayout) view.findViewById(R.id.tweet_linear_layout);
//        for (Tweet tweet: tweets) {
//            TextView temp = new TextView(getContext());
//            temp.setText(tweet.getUser().getDisplayedName() + "\n" + tweet.getText());
//            ll.addView(temp);
//        }
//    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NewsViewModel.class);
        // TODO: Use the ViewModel
    }



}