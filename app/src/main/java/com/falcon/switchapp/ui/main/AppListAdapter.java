package com.falcon.switchapp.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.falcon.switchapp.AppAlternateListActivity;
import com.falcon.switchapp.R;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.MyViewHolder> {
    private List<AppListViewModel.DetectedAppViewModel> originalDataSet;
    private List<AppListViewModel.DetectedAppViewModel> mDataset;
    private Activity mActivity;
    private AppListViewModel.Country mFilterByCountry = AppListViewModel.Country.All;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public MyViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AppListAdapter(List<AppListViewModel.DetectedAppViewModel> myDataset, Activity activity) {
        originalDataSet = myDataset;
        mDataset = new ArrayList<>(originalDataSet);
        mActivity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AppListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_row, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        ((TextView)holder.view.findViewById(R.id.name)).setText(mDataset.get(position).name);
        ((TextView)holder.view.findViewById(R.id.description)).setText(mDataset.get(position).description);
        if (mDataset.get(position).drawable == null) {
            Glide.with(mActivity).load(mDataset.get(position).iconUri).into((ImageView)holder.view.findViewById(R.id.image));
        } else {
            Glide.with(mActivity).load(mDataset.get(position).drawable).into((ImageView)holder.view.findViewById(R.id.image));
        }
        if (mDataset.get(position).link == null) {
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(
                            "https://play.google.com/store/apps/details?id="+mDataset.get(position).id));
                    intent.setPackage("com.android.vending");
                    holder.view.getContext().startActivity(intent);
                }
            });
        } else {
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(mDataset.get(position).link));
                    holder.view.getContext().startActivity(intent);
                }
            });
        }
        if (mDataset.get(position).alternateApps.size() > 0) {
            holder.view.findViewById(R.id.alternate_button).setVisibility(View.VISIBLE);
            holder.view.findViewById(R.id.alternate_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mActivity, AppAlternateListActivity.class);
                    intent.putParcelableArrayListExtra("alternateApps", mDataset.get(position).alternateApps);
                    mActivity.startActivity(intent);
                }
            });
        } else {
            holder.view.findViewById(R.id.alternate_button).setVisibility(View.INVISIBLE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setFilter(AppListViewModel.Country country)  {
        if(country != mFilterByCountry) {
            mFilterByCountry = country;
            mDataset.clear();
            if(country == AppListViewModel.Country.All) {
                mDataset = new ArrayList<>(originalDataSet);
            } else {
                for (AppListViewModel.DetectedAppViewModel viewModel : originalDataSet) {
                    if (viewModel.country == country) {
                        mDataset.add(viewModel);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    public void injectAd(int i, AppListViewModel.DetectedAppViewModel ad) {
        originalDataSet.add(i, ad);
        if (mFilterByCountry == AppListViewModel.Country.All) {
            mDataset.add(i, ad);
            notifyDataSetChanged();
        }
    }
}
