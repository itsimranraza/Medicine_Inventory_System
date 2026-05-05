package com.example.medicineinventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ManageMedicineActivity extends AppCompatActivity {

    Button buttonGoToAdd, buttonGoToUpdate, buttonGoToDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_medicine);

        buttonGoToAdd = findViewById(R.id.buttonGoToAdd);
        buttonGoToUpdate = findViewById(R.id.buttonGoToUpdate);
        buttonGoToDelete = findViewById(R.id.buttonGoToDelete);

        buttonGoToAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddMedicineActivity.class));
        });

        buttonGoToUpdate.setOnClickListener(v -> {
            startActivity(new Intent(this, UpdateMedicineActivity.class));
        });

        buttonGoToDelete.setOnClickListener(v -> {
            startActivity(new Intent(this, DeleteMedicineActivity.class));
        });
    }
}
