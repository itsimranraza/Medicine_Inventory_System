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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StockCheckActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MedicineStockAdapter stockAdapter;
    ArrayList<Medicine> medicineList;
    DatabaseReference medicineRef;
    ValueEventListener stockListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_check);

        recyclerView = findViewById(R.id.recyclerViewStock);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        medicineList = new ArrayList<>();

        stockAdapter = new MedicineStockAdapter(this, medicineList);
        recyclerView.setAdapter(stockAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return; // skip loading if no user
        }

        String uid = user.getUid();
        medicineRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("medicines");

        loadStockData();
    }

    private void loadStockData() {
        stockListener = medicineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicineList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Medicine med = snap.getValue(Medicine.class);
                    if (med != null) {
                        medicineList.add(med);
                    }
                }

                Collections.sort(medicineList, Comparator.comparingInt(Medicine::getQuantity));
                stockAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StockCheckActivity.this, "Failed to load stock data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (medicineRef != null && stockListener != null) {
            medicineRef.removeEventListener(stockListener);
        }
    }
}
