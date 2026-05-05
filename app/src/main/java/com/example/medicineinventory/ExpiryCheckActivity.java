package com.example.medicineinventory;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class ExpiryCheckActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MedicineExpiryAdapter expiryAdapter;
    ArrayList<Medicine> medicineList;
    DatabaseReference medicineRef;
    ValueEventListener expiryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expiry_check);

        recyclerView = findViewById(R.id.recyclerViewExpiry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        medicineList = new ArrayList<>();
        expiryAdapter = new MedicineExpiryAdapter(this, medicineList);
        recyclerView.setAdapter(expiryAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        medicineRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("medicines");

        loadExpiryData();
    }

    private void loadExpiryData() {
        expiryListener = medicineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicineList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Medicine med = snap.getValue(Medicine.class);
                    if (med != null && med.getExpiryDate() != null) {
                        medicineList.add(med);
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Collections.sort(medicineList, (m1, m2) -> {
                    try {
                        Date d1 = sdf.parse(m1.getExpiryDate());
                        Date d2 = sdf.parse(m2.getExpiryDate());
                        return d1.compareTo(d2);
                    } catch (Exception e) {
                        return 0;
                    }
                });

                expiryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExpiryCheckActivity.this, "Failed to load expiry data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (medicineRef != null && expiryListener != null) {
            medicineRef.removeEventListener(expiryListener);
        }
    }
}
