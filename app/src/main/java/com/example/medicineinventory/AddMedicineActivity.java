package com.example.medicineinventory;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class AddMedicineActivity extends AppCompatActivity {

    EditText editName, editBatchNo, editManufacturer, editQuantity, editExpiryDate, editPrice, editCategory;
    Button btnAddMedicine;
    DatabaseReference medicineRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        editName = findViewById(R.id.editName);
        editBatchNo = findViewById(R.id.editBatchNo);
        editManufacturer = findViewById(R.id.editManufacturer);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiryDate = findViewById(R.id.editExpiryDate);
        editPrice = findViewById(R.id.editPrice);
        editCategory = findViewById(R.id.editCategory);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        medicineRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("medicines");

        btnAddMedicine.setOnClickListener(v -> addMedicine());
    }

    private void addMedicine() {
        String name = editName.getText().toString();
        String batch = editBatchNo.getText().toString();
        String manufacturer = editManufacturer.getText().toString();
        String expiry = editExpiryDate.getText().toString();
        String category = editCategory.getText().toString();

        int quantity;
        double price;

        try {
            quantity = Integer.parseInt(editQuantity.getText().toString());
            price = Double.parseDouble(editPrice.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity or price", Toast.LENGTH_SHORT).show();
            return;
        }

        if (batch.isEmpty()) {
            Toast.makeText(this, "Batch number is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> medicineData = new HashMap<>();
        medicineData.put("name", name);
        medicineData.put("batchNo", batch);
        medicineData.put("manufacturer", manufacturer);
        medicineData.put("quantity", quantity);
        medicineData.put("expiryDate", expiry);
        medicineData.put("price", price);
        medicineData.put("category", category);

        medicineRef.child(batch).setValue(medicineData)
                .addOnSuccessListener(aVoid ->{
                        Toast.makeText(this, "Medicine added", Toast.LENGTH_SHORT).show();
                        finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
