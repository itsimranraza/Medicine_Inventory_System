package com.example.medicineinventory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class InventoryActivity extends AppCompatActivity {

    Button buttonSellMedicine, buttonManageMedicine, buttonCheckStock, buttonCheckExpiry, btnLogout, btnChangePassword;
    TextView textUsername;

    FirebaseAuth mAuth;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);

        textUsername = findViewById(R.id.textUsername);
        buttonSellMedicine = findViewById(R.id.buttonSellMedicine);
        buttonManageMedicine = findViewById(R.id.buttonManageMedicine);
        buttonCheckStock = findViewById(R.id.buttonCheckStock);
        buttonCheckExpiry = findViewById(R.id.buttonCheckExpiry);
        btnLogout = findViewById(R.id.btnLogout);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            userRef.child(uid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String username = snapshot.getValue(String.class);
                    if (username != null) {
                        textUsername.setText("Welcome, " + username);
                    } else {
                        textUsername.setText("Welcome, User");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    textUsername.setText("Welcome");
                }
            });
        }

        buttonSellMedicine.setOnClickListener(v ->
                startActivity(new Intent(this, SellMedicineActivity.class)));

        buttonManageMedicine.setOnClickListener(v ->
                startActivity(new Intent(this, ManageMedicineActivity.class)));

        buttonCheckStock.setOnClickListener(v ->
                startActivity(new Intent(this, StockCheckActivity.class)));

        buttonCheckExpiry.setOnClickListener(v ->
                startActivity(new Intent(this, ExpiryCheckActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
