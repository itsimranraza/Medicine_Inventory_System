package com.example.medicineinventory;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class DeleteMedicineActivity extends AppCompatActivity {

    Spinner spinnerDelete;
    Button btnDelete;

    DatabaseReference medicineRef;
    FirebaseAuth mAuth;
    Map<String, String> batchMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_medicine);

        spinnerDelete = findViewById(R.id.spinnerMedicines);
        btnDelete = findViewById(R.id.btnDelete);

        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        medicineRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("medicines");

        loadMedicines();

        btnDelete.setOnClickListener(v -> {
            String selected = spinnerDelete.getSelectedItem().toString();
            String batchNo = batchMap.get(selected);
            if (batchNo != null) {
                medicineRef.child(batchNo).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Medicine deleted", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
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
                ArrayAdapter<String> adapter = new ArrayAdapter<>(DeleteMedicineActivity.this, android.R.layout.simple_spinner_dropdown_item, names);
                spinnerDelete.setAdapter(adapter);
            }

            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
