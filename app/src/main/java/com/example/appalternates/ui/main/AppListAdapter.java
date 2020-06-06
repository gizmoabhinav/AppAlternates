package com.example.appalternates.ui.main;

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
import com.example.appalternates.AppAlternateListActivity;
import com.example.appalternates.R;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.MyViewHolder> {
    private ArrayList<AppListViewModel.DetectedAppViewModel> mDataset;
    private Activity mActivity;

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
    public AppListAdapter(ArrayList<AppListViewModel.DetectedAppViewModel> myDataset, Activity activity) {
        mDataset = myDataset;
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
        Glide.with(mActivity).load(mDataset.get(position).iconUri).into((ImageView)holder.view.findViewById(R.id.image));
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
}
