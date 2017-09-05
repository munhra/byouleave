package com.example.vntraal.byouleave;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Handler;

/**
 * Created by vntjoig on 23/08/2017.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder>{

    private ArrayList<String> mData = new ArrayList<String>();
    public View.OnClickListener mClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View eventView;
        public TextView appointment;
        public ImageView arrowAnimation;

        public ViewHolder(View v){
            super(v);
            eventView = v;
            appointment = (TextView) v.findViewById(R.id.event);

            final Handler handler = new Handler();
            Random r = new Random();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    arrowAnimation = (ImageView) eventView.findViewById(R.id.arrowAnimation);
                    arrowAnimation.setBackgroundResource(R.drawable.arrow_animation);
                    AnimationDrawable anim = (AnimationDrawable) arrowAnimation.getBackground();
                    anim.start();
                }
            }, r.nextInt(1000 - 0));

        }

    }

    public void setmData(ArrayList<String> received){
        mData = received;
        notifyDataSetChanged();
        for (String event : mData){
            Log.e("Member name: ", event);
        }

    }

    public void resetData(){
        mData = new ArrayList<String>();
        notifyDataSetChanged();
    }

    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(EventAdapter.ViewHolder holder, int position) {
        holder.appointment.setText(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
