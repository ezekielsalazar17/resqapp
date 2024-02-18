package com.example.resqapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class Ocr extends AppCompatActivity {

    private ImageView imageView6;
    private TextView ocrconverted;
    private Button buttonUpload;
    FloatingActionButton selectimg;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICKER = 2;
    Uri imageUri;
    TextRecognizer textRecognizer;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr);

        buttonUpload = findViewById(R.id.upload);
        selectimg = findViewById(R.id.selectimg);
        ocrconverted = findViewById(R.id.ocrconverted);
        imageView6 = findViewById(R.id.imageView6);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        selectimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(Ocr.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICKER) {
            if (resultCode == RESULT_OK && data != null) {
                imageUri = data.getData();
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                recognizeText();
            } else {
                Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void recognizeText() {
        if (imageUri != null) {
            try {
                InputImage image = InputImage.fromFilePath(Ocr.this, imageUri);

                textRecognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
                                ocrconverted.setText(visionText.getText());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                // Task failed with an exception
                                // ...
                                Toast.makeText(Ocr.this, "Failed to recognize text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IOException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
