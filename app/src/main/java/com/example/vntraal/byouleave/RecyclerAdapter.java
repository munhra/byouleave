package com.example.vntraal.byouleave;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vntants on 8/17/17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    private List<String> mCalendarData;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public TextView mEvent;
//        public TextView mDescription;
//        public TextView mLocation;
//        public TextView mStart;

        public ViewHolder(View v) {
            super(v);
            itemView = v;
            mEvent = (TextView) v.findViewById(R.id.event);
//            mDescription = (TextView) v.findViewById(R.id.description);
//            mLocation = (TextView) v.findViewById(R.id.location);
//            mStart = (TextView) v.findViewById(R.id.start);
        }
    }

    public RecyclerAdapter() { mCalendarData = new ArrayList<>(); }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            String event = mCalendarData.get(position);
            Log.v("RecyclerAdapter java",event);
            holder.mEvent.setText(event);
        } catch (Exception e) {
            //Error
        }
    }

    @Override
    public int getItemCount() {
        return mCalendarData.size();
    }

}
