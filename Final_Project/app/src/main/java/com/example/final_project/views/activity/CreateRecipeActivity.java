package com.example.final_project.views.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_project.R;
import java.io.IOException;

public class CreateRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etRecipeName, etRecipeDescription, etRecipeNote;
    private ImageView imageRecipePreview;
    private Button btnChooseImage, btnSaveRecipe;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        etRecipeName = findViewById(R.id.etRecipeName);
        etRecipeDescription = findViewById(R.id.etRecipeDescription);
        etRecipeNote = findViewById(R.id.etRecipeNote);
        imageRecipePreview = findViewById(R.id.imageRecipePreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnSaveRecipe = findViewById(R.id.btnSaveRecipe);

        btnChooseImage.setOnClickListener(v -> openImagePicker());
        btnSaveRecipe.setOnClickListener(v -> saveRecipe());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Recipe Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageRecipePreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveRecipe() {
        String name = etRecipeName.getText().toString().trim();
        String description = etRecipeDescription.getText().toString().trim();
        String note = etRecipeNote.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Recipe '" + name + "' created successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
