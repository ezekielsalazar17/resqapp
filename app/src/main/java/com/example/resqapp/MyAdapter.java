package com.example.resqapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private Context context;
    private List<Item> items;

    public MyAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Check if context is null before inflating the layout
        if (context == null) {
            throw new NullPointerException("Context is null. Make sure it is properly initialized.");
        }
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item currentItem = items.get(position);
        holder.nameView.setText("Name: " + currentItem.getFirstName() + " " + currentItem.getLastName());
        holder.addressView.setText("Address: " + currentItem.getAddress());
        holder.latitudeView.setText("Latitude: " + (currentItem.getLatitude()));
        holder.longitudeView.setText("Longitude: " + (currentItem.getLongitude()));
        holder.contactnumView.setText("Contact Number: " + (currentItem.getContactNum()));
        holder.checkBox.setImageResource(R.drawable.baseline_check_24);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
