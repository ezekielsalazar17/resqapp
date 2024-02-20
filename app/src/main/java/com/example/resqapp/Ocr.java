package com.example.resqapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.IOException;

public class Ocr extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICKER = 1;
    EditText ocrconverted;
    ImageView selectimg;
    ImageView image;
    Uri imageUri;
    TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr);

        selectimg = findViewById(R.id.selectimg);
        ocrconverted = findViewById(R.id.ocrconverted);
        image = findViewById(R.id.imageView6); // Add a semicolon here
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        selectimg.setOnClickListener(v -> ImagePicker.with(Ocr.this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICKER) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    image.setImageURI(imageUri); // Change imageView6 to image
                    Toast.makeText(this, "Image Selected", Toast.LENGTH_SHORT).show();
                    recognizeText();
                } else {
                    Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to pick image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void recognizeText() {
        if (imageUri != null) {
            try {
                InputImage inputImage = InputImage.fromFilePath(this, imageUri);

                textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                String recognizeText = text.getText();
                                ocrconverted.setText(recognizeText);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Ocr.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IOException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}