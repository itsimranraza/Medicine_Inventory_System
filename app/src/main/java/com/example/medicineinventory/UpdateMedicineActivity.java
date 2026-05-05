package com.example.medicineinventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class UpdateMedicineActivity extends AppCompatActivity {

    Spinner spinnerMedicines;
    EditText editName, editManufacturer, editQuantity, editExpiryDate, editPrice, editCategory;
    Button btnUpdate;

    DatabaseReference medicineRef;
    FirebaseAuth mAuth;
    Map<String, String> batchMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_medicine);

        spinnerMedicines = findViewById(R.id.spinnerMedicines);
        editName = findViewById(R.id.editName);
        editManufacturer = findViewById(R.id.editManufacturer);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiryDate = findViewById(R.id.editExpiryDate);
        editPrice = findViewById(R.id.editPrice);
        editCategory = findViewById(R.id.editCategory);
        btnUpdate = findViewById(R.id.btnUpdate);

        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        medicineRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("medicines");

        loadMedicines();

        spinnerMedicines.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedName = spinnerMedicines.getSelectedItem().toString();
                String batchNo = batchMap.get(selectedName);
                if (batchNo != null) {
                    medicineRef.child(batchNo).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(DataSnapshot snapshot) {
                            editName.setText(snapshot.child("name").getValue(String.class));
                            editManufacturer.setText(snapshot.child("manufacturer").getValue(String.class));
                            editQuantity.setText(String.valueOf(snapshot.child("quantity").getValue(Integer.class)));
                            editExpiryDate.setText(snapshot.child("expiryDate").getValue(String.class));
                            editPrice.setText(String.valueOf(snapshot.child("price").getValue(Double.class)));
                            editCategory.setText(snapshot.child("category").getValue(String.class));
                        }
                        @Override public void onCancelled(DatabaseError error) {}
                    });
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnUpdate.setOnClickListener(v -> {
            String selectedName = spinnerMedicines.getSelectedItem().toString();
            String batchNo = batchMap.get(selectedName);
            if (batchNo == null) return;

            Map<String, Object> updated = new HashMap<>();
            updated.put("name", editName.getText().toString());
            updated.put("manufacturer", editManufacturer.getText().toString());
            updated.put("quantity", Integer.parseInt(editQuantity.getText().toString()));
            updated.put("expiryDate", editExpiryDate.getText().toString());
            updated.put("price", Double.parseDouble(editPrice.getText().toString()));
            updated.put("category", editCategory.getText().toString());

            medicineRef.child(batchNo).updateChildren(updated)
                    .addOnSuccessListener(aVoid ->{ Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, InventoryActivity.class));
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loadMedicines() {
        medicineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                List<String> names = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String name = snap.child("name").getValue(String.class);
                    String batch = snap.child("batchNo").getValue(String.class);
                    if (name != null && batch != null) {
                        names.add(name);
                        batchMap.put(name, batch);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(UpdateMedicineActivity.this, android.R.layout.simple_spinner_dropdown_item, names);
                spinnerMedicines.setAdapter(adapter);
            }

            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
