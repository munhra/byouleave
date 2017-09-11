package com.example.vntraal.byouleave;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vntjoig on 23/08/2017.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private ArrayList<String> mData = new ArrayList<String>();

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View eventView;
        public TextView appointment;

        public ViewHolder(View v){
            super(v);
            eventView = v;
            appointment = (TextView) v.findViewById(R.id.event);
        }
    }

    public void setmData(ArrayList<String> received){
        mData = received;
        notifyDataSetChanged();
        for (String event : mData){
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
