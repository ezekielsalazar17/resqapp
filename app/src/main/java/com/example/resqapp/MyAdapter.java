package com.example.resqapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item currentItem = items.get(position);
        holder.nameView.setText("Name: " + currentItem.getFirstName() + " " + currentItem.getLastName());
        holder.addressView.setText("Address: " + currentItem.getAddress());
        holder.latitudeView.setText("Latitude: " + (currentItem.getLatitude()));
        holder.longitudeView.setText("Longitude: " + (currentItem.getLongitude()));
        holder.contactnumView.setText("Contact Number: " + (currentItem.getContactNum()));

        // Set OnClickListener for the ImageButton
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle item click
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Item clickedItem = items.get(adapterPosition);
                    Intent intent = new Intent(context, LocationSharingAdmin.class);
                    intent.putExtra("Address", clickedItem.getAddress());
                    context.startActivity(intent); // Start activity using context
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
}
