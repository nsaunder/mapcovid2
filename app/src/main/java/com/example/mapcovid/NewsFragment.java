package com.example.mapcovid;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.mapcovid.ui.news.NewsViewModel;
import com.github.redouane59.twitter.dto.tweet.Tweet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class NewsFragment extends Fragment {
    private NewsViewModel mViewModel;
    public static NewsFragment newInstance() {
        return new NewsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ArrayList<Tweet> tweets_mainview = new ArrayList<Tweet>();
        TwitterFilteredStream t1 = new TwitterFilteredStream();
        t1.start();

        View view = inflater.inflate(R.layout.news_fragment, container, false);

        t1.addListeners(new TweetListener() {
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
                               temp.setText("@" + tweet.getUser().getName() + "\n\n" + tweet.getText() + "\n\n" + tweet.getCreatedAt().getMonth() + " " + tweet.getCreatedAt().getDayOfMonth()+", "+tweet.getCreatedAt().getYear());
                               temp.setBackgroundColor(Color.GRAY);
                               temp.setBackgroundResource(R.drawable.back);
                               temp.setLayoutParams(params);
                        }
                    });
                    ll.post(new Runnable() {
                        @Override
                        public void run() {
                            ll.addView(temp);
                        }
                    });
                    System.out.println(tweet.getText());
                }
            }
        });
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