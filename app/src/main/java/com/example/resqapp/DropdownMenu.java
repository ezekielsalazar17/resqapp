package com.example.resqapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DropdownMenu extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner spinner;
    private AdapterView<Adapter> adapterView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_register);

        spinner = findViewById(R.id.dropdown_menu1);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.department, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String department = adapterView.getItemAtPosition(position).toString();
        Toast.makeText(getApplicationContext(), department,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

