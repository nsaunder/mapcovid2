package com.example.mapcovid;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.mapcovid.ui.path.PathViewModel;

public class PathFragment extends Fragment {
    private Button b1;
    private PathViewModel mViewModel;
    Constant c;
    public static PathFragment newInstance() {
        return new PathFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        c = new Constant();
        c.readFile(getContext());
        return inflater.inflate(R.layout.fragment_path, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PathViewModel.class);
        // TODO: Use the ViewModel
    }

}