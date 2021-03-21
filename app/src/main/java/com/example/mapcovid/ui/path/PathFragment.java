package com.example.mapcovid.ui.path;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.example.mapcovid.R;

public class PathFragment extends Fragment {

    private PathViewModel mViewModel;

    public static PathFragment newInstance() {
        return new PathFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_path, container, false);
    }
    public void setDate(View view){
        DatePicker dp =(DatePicker)view.findViewById(R.id.datePicker);

        int day = dp.getDayOfMonth();
        int month = dp.getMonth()+1;
        int year = dp.getYear();

        //selectedDate = year + "-"+ month + "-" + day;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PathViewModel.class);
        // TODO: Use the ViewModel
    }

}