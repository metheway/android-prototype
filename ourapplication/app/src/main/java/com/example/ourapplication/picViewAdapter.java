package com.example.ourapplication;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 10065 on 2018/10/29.
 */

public class picViewAdapter extends RecyclerView.Adapter<picViewAdapter.ViewHolder> {

    private Context mContext;

    private List<picSet> mPicSet;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView picUri;
        TextView picDate;

        public ViewHolder(View view){
            super(view);
            cardView = (CardView)view;
            picUri = (ImageView)view.findViewById(R.id.imageuri);
            picDate = (TextView)view.findViewById(R.id.imagedate);
        }
    }

    public picViewAdapter(List<picSet> picSet){
        mPicSet = picSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.recommand_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        picSet mPic = mPicSet.get(position);
        holder.picUri.setImageResource(mPic.getImageUri());
        holder.picDate.setText(mPic.getDate());
    }

    @Override
    public int getItemCount() {
        return mPicSet.size();
    }
}
