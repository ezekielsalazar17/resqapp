package com.example.resqapp;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    TextView nameView;
    TextView addressView;
    TextView latitudeView;
    TextView longitudeView;
    TextView contactnumView;
    TextView addressadminView;
    TextView latitudeadminView;
    TextView longitudeadminView;
    TextView useremailView;
    TextView timestampView;
    ImageButton checkBox;
    ImageView pictureuser;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        useremailView = itemView.findViewById(R.id.useremail);
        nameView = itemView.findViewById(R.id.name);
        addressView = itemView.findViewById(R.id.address);
        latitudeView = itemView.findViewById(R.id.latitude);
        longitudeView = itemView.findViewById(R.id.longitude);
        contactnumView = itemView.findViewById(R.id.contact_number);
        addressadminView = itemView.findViewById(R.id.address_admin);
        latitudeadminView = itemView.findViewById(R.id.latitude_admin);
        longitudeadminView = itemView.findViewById(R.id.longitude_admin);
        timestampView = itemView.findViewById(R.id.timestamp);
        checkBox = itemView.findViewById(R.id.accept_button);
        pictureuser = itemView.findViewById(R.id.picture_user);
    }

}
