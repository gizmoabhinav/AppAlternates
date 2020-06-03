package com.example.appalternates.ui.main;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.appalternates.R;
import com.inmobi.ads.InMobiBanner;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;
    private AppListAdapter mAdapter;
    private List<String[]> mAppList;
    private InMobiBanner bannerAd;
    private static MainFragment instance;

    public static MainFragment newInstance() {
        instance = new MainFragment();
        return instance;
    }

    public static MainFragment getInstance() {
        return instance;
    }

    public void loadAd() {
        bannerAd.load();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_fragment, container, false);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        RecyclerView recyclerView = root.findViewById(R.id.app_list);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAppList = new ArrayList<>();

        // specify an adapter (see also next example)
        mAdapter = new AppListAdapter(mAppList);
        recyclerView.setAdapter(mAdapter);

        bannerAd = root.findViewById(R.id.banner);
        bannerAd.load();
        mViewModel.fetchLatestList(mAppList, mAdapter, getContext().getPackageManager());
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }



}
