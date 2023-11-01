package com.example.resqapp.Utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.example.resqapp.R;
import com.example.resqapp.Offline1;

public class NetworkChangeListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!detect_internet.isConnectedToInternet(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View layout_dialog = LayoutInflater.from(context).inflate(R.layout.network_detection, null);
            builder.setView(layout_dialog);

            Button btnRetry = layout_dialog.findViewById(R.id.btnRetry);
            Button offlinemode = layout_dialog.findViewById(R.id.offlinemode);

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCancelable(true);

            dialog.getWindow().setGravity(Gravity.CENTER);

            btnRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    onReceive(context, intent);
                }
            });

            offlinemode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent offlineIntent = new Intent(context, Offline1.class);
                    context.startActivity(offlineIntent);
                }
            });

    }
}
}
