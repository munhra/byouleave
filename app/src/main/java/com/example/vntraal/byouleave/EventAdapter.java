package com.example.vntraal.byouleave;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Handler;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vntjoig on 23/08/2017.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder>{

    private ArrayList<String> mData = new ArrayList<String>();
    public View.OnClickListener mClickListener;


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View eventView;
        public TextView appointment;
        public TextView initialTime;
        public TextView eventDescription;
        public RelativeLayout backgroundTime;

        public ViewHolder(View v){
            super(v);
            eventView = v;
            appointment = (TextView) v.findViewById(R.id.eventName);
            initialTime = (TextView) v.findViewById(R.id.time);
            eventDescription = (TextView) v.findViewById(R.id.description);
            backgroundTime = (RelativeLayout) v.findViewById(R.id.timeheader);
        }

    }

    public void setmData(ArrayList<String> received){
        mData = received;
        notifyDataSetChanged();

    }

    public void resetData(){
        mData = new ArrayList<String>();
        notifyDataSetChanged();
    }

    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.updated_recycler_item, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(EventAdapter.ViewHolder holder, int position) {
        if(position%2==0){
            holder.backgroundTime.setBackgroundColor(Color.parseColor("#5FDDFF"));

        } else {
            holder.backgroundTime.setBackgroundColor(Color.parseColor("#88E8FD"));

        }

        try {
            JSONObject json =new JSONObject(mData.get(position));

            Log.e("Data Analysis",json.get("eventName").toString() + " = " + position);

            if(json.has("time")){
                holder.initialTime.setText(json.get("time").toString());
            } else holder.initialTime.setText("");
            if(json.has("eventName")){
                holder.appointment.setText(json.get("eventName").toString());
            } else holder.initialTime.setText("");
            if(json.has("description")){
                if((json.isNull("description")) || (json.get("description").toString().equals("null"))){
                    holder.eventDescription.setText("Nenhuma descrição disponivel");
                }

            } else holder.initialTime.setText("");


        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("STOP JSON",e + "");
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
