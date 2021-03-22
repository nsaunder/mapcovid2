package com.example.mapcovid;

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
                for (Tweet tweet : t1.getTweets()) {
                    TextView temp = new TextView(getContext());
                    temp.post(new Runnable() {
                        @Override
                        public void run() {
                            temp.setText(tweet.getUser().getDisplayedName() + "\n" + tweet.getText());
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