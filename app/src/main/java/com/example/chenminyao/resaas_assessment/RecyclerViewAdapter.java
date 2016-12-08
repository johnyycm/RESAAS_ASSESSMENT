package com.example.chenminyao.resaas_assessment;

import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

/**
 * Created by chenminyao on 2016-12-07.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<Listing> shopListings;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name, address, rating;
        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tv_shop_name);
            address = (TextView) v.findViewById(R.id.tv_address);
            rating = (TextView) v.findViewById(R.id.tv_rating);
        }
    }

    public RecyclerViewAdapter(ArrayList<Listing> list, MainActivity activity){
        shopListings = list;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listing_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.name.setText(shopListings.get(position).getName());
        holder.address.setText(shopListings.get(position).getAddress());
        holder.rating.setText(shopListings.get(position).getUserRating());    }

    @Override
    public int getItemCount() {
        return shopListings.size() ;
    }
}

