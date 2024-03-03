package com.example.resqapp;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    TextView nameView, addressView, latitudeView, longitudeView, contactnumView;
    ImageButton checkBox;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        nameView = itemView.findViewById(R.id.name);
        addressView = itemView.findViewById(R.id.address);
        latitudeView = itemView.findViewById(R.id.latitude);
        longitudeView = itemView.findViewById(R.id.longitude);
        contactnumView = itemView.findViewById(R.id.contact_number);
        checkBox = itemView.findViewById(R.id.accept_button);
    }
}
