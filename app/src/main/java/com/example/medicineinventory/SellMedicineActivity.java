package com.example.medicineinventory;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class SellMedicineActivity extends AppCompatActivity {

    private LinearLayout entryContainer;
    private Button addEntryButton, sellButton;
    private TextView grandTotalText;

    private final Map<String, DataSnapshot> medicineMap = new HashMap<>();
    private final List<String> selectedBatchNos = new ArrayList<>();
    private final List<TextWatcher> textWatchers = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference medicineRef, salesRef;

    private boolean isUpdatingSpinners = false;

    private static final int CREATE_FILE_REQUEST_CODE = 101;
    private byte[] pendingPdfBytes = null;
    private String pendingFileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_medicine);

        entryContainer = findViewById(R.id.entryContainer);
        addEntryButton = findViewById(R.id.addEntryButton);
        sellButton = findViewById(R.id.sellButton);
        grandTotalText = findViewById(R.id.textFinalTotal);

        String uid = mAuth.getCurrentUser().getUid();
        medicineRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("medicines");
        salesRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("sales");

        fetchMedicines();

        addEntryButton.setOnClickListener(v -> addNewEntry());
        sellButton.setOnClickListener(v -> processSale());
    }

    private void fetchMedicines() {
        medicineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicineMap.clear();
                for (DataSnapshot medSnap : snapshot.getChildren()) {
                    String expiry = medSnap.child("expiryDate").getValue(String.class);
                    if (!isExpired(expiry)) {
                        String batchNo = medSnap.child("batchNo").getValue(String.class);
                        if (batchNo != null) {
                            medicineMap.put(batchNo, medSnap);
                        }
                    }
                }
                if (medicineMap.isEmpty()) {
                    Toast.makeText(SellMedicineActivity.this, "No valid medicines available.", Toast.LENGTH_LONG).show();
                    addEntryButton.setEnabled(false);
                    sellButton.setEnabled(false);
                    return;
                }
                addNewEntry();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SellMedicineActivity.this, "Failed to load medicines", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isExpired(String expiryDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiry = sdf.parse(expiryDate);
            return expiry.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private List<String> getAvailableBatchNos(String currentBatchNo) {
        List<String> availableBatchNos = new ArrayList<>();

        for (String batchNo : medicineMap.keySet()) {
            if (!selectedBatchNos.contains(batchNo) || batchNo.equals(currentBatchNo)) {
                availableBatchNos.add(batchNo);
            }
        }

        if (availableBatchNos.size() == 1 &&
                !availableBatchNos.get(0).equals(currentBatchNo) &&
                selectedBatchNos.contains(availableBatchNos.get(0))) {
            availableBatchNos.clear();
        }

        Collections.sort(availableBatchNos);
        return availableBatchNos;
    }

    private String getMedicineDisplayName(String batchNo) {
        DataSnapshot snap = medicineMap.get(batchNo);
        if (snap != null) {
            String name = snap.child("name").getValue(String.class);
            String manufacturer = snap.child("manufacturer").getValue(String.class);
            return (name != null ? name : "Unknown") + " (" + batchNo + ")" +
                    (manufacturer != null ? " - " + manufacturer : "");
        }
        return batchNo;
    }

    private String extractBatchNoFromDisplay(String display) {
        if (display == null || display.isEmpty()) {
            return null;
        }


        int start = display.indexOf('(');
        int end = display.indexOf(')');

        if (start >= 0 && end > start && end <= display.length()) {
            return display.substring(start + 1, end);
        }


        if (display.matches("B\\d+")) {
            return display;
        }

        return null;
    }

    private void addNewEntry() {
        List<String> availableBatchNos = getAvailableBatchNos(null);

        if (availableBatchNos.isEmpty()) {
            Toast.makeText(this, "All medicines are already in use", Toast.LENGTH_SHORT).show();
            addEntryButton.setEnabled(false);
            return;
        }

        View entryView = LayoutInflater.from(this).inflate(R.layout.item_sell_entry, entryContainer, false);
        Spinner spinner = entryView.findViewById(R.id.spinnerMedicine);
        EditText qtyInput = entryView.findViewById(R.id.editQuantity);


        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int idx = entryContainer.indexOfChild(entryView);
                if (idx >= 0 && idx < selectedBatchNos.size()) {
                    updateEntryTotals(entryView);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        qtyInput.addTextChangedListener(watcher);
        textWatchers.add(watcher);

        selectedBatchNos.add(null);
        entryContainer.addView(entryView);

        setupSpinner(spinner, entryContainer.getChildCount());
        updateAllSpinnersAdapters();
    }

    private void setupSpinner(Spinner spinner, int entryIndex) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingSpinners) return;

                String selected = (String) parent.getItemAtPosition(position);
                if (selected == null || selected.equals("No medicines available")) {
                    return;
                }

                String batchNo = extractBatchNoFromDisplay(selected);
                if (batchNo == null) {
                    Toast.makeText(SellMedicineActivity.this,
                            "Could not identify medicine", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentBatchNo = selectedBatchNos.get(entryIndex - 1);

                if (!Objects.equals(currentBatchNo, batchNo)) {
                    if (!selectedBatchNos.contains(batchNo)) {
                        selectedBatchNos.set(entryIndex - 1, batchNo);
                        updateAllSpinnersAdapters();
                        // Force immediate update for the current entry
                        View entryView = entryContainer.getChildAt(entryIndex - 1);
                        if (entryView != null) {
                            updateEntryTotals(entryView);
                        }
                    } else {
                        updateAllSpinnersAdapters();
                    }
                }
                validateAllEntries();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateAllSpinnersAdapters() {
        isUpdatingSpinners = true;

        Set<String> currentlySelected = new HashSet<>();
        for (String batchNo : selectedBatchNos) {
            if (batchNo != null) {
                currentlySelected.add(batchNo);
            }
        }

        for (int i = 0; i < entryContainer.getChildCount(); i++) {
            View entryView = entryContainer.getChildAt(i);
            Spinner spinner = entryView.findViewById(R.id.spinnerMedicine);

            String currentBatchNo = selectedBatchNos.get(i);
            List<String> availableBatchNos = getAvailableBatchNos(currentBatchNo);

            if (currentBatchNo != null &&
                    (!availableBatchNos.contains(currentBatchNo) ||
                            Collections.frequency(selectedBatchNos, currentBatchNo) > 1)) {
                selectedBatchNos.set(i, null);
                currentBatchNo = null;
            }

            List<String> displayList = new ArrayList<>();
            for (String batchNo : availableBatchNos) {
                displayList.add(getMedicineDisplayName(batchNo));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    displayList.isEmpty() ? Collections.singletonList("No medicines available") : displayList
            );
            spinner.setAdapter(adapter);

            if (currentBatchNo != null) {
                int position = availableBatchNos.indexOf(currentBatchNo);
                if (position >= 0) {
                    spinner.setSelection(position, false);
                    updateEntryTotals(entryView);
                }
            } else if (!availableBatchNos.isEmpty()) {
                spinner.setSelection(0, false);
                selectedBatchNos.set(i, availableBatchNos.get(0));
                currentlySelected.add(availableBatchNos.get(0));
                updateEntryTotals(entryView);
            } else {
                entryContainer.removeView(entryView);
                selectedBatchNos.remove(i);
                i--;
            }
        }

        isUpdatingSpinners = false;
        validateAllEntries();
        addEntryButton.setEnabled(!getAvailableBatchNos(null).isEmpty());
    }

    private void updateEntryTotals(View entryView) {
        Spinner spinner = entryView.findViewById(R.id.spinnerMedicine);
        EditText qtyInput = entryView.findViewById(R.id.editQuantity);
        TextView txtRemaining = entryView.findViewById(R.id.txtRemainingStock);
        TextView txtUnitPrice = entryView.findViewById(R.id.txtUnitPrice);
        TextView txtTotal = entryView.findViewById(R.id.txtTotalPrice);

        int idx = entryContainer.indexOfChild(entryView);
        if (idx < 0 || idx >= selectedBatchNos.size()) return;

        String batchNo = selectedBatchNos.get(idx);
        if (batchNo == null || !medicineMap.containsKey(batchNo)) {
            txtUnitPrice.setText("Unit Price: -");
            txtTotal.setText("Total: ৳0.00");
            txtRemaining.setText("Available: -");
            updateGrandTotal();
            return;
        }

        DataSnapshot snap = medicineMap.get(batchNo);
        Integer availableQty = snap.child("quantity").getValue(Integer.class);
        Double unitPrice = snap.child("price").getValue(Double.class);

        if (availableQty == null) availableQty = 0;
        if (unitPrice == null) unitPrice = 0.0;


        txtUnitPrice.setText(String.format(Locale.US, "Unit Price: ৳%.2f", unitPrice));
        txtRemaining.setText(String.format(Locale.US, "Available: %d", availableQty));


        int qtyToSell = 0;
        try {
            if (!qtyInput.getText().toString().isEmpty()) {
                qtyToSell = Integer.parseInt(qtyInput.getText().toString());
            }
        } catch (NumberFormatException e) {
            qtyInput.setError("Invalid quantity");
            txtTotal.setText("Total: ৳0.00");
            updateGrandTotal();
            validateAllEntries();
            return;
        }

        if (qtyToSell > availableQty) {
            qtyInput.setError("Only " + availableQty + " available");
        } else if (qtyToSell <= 0 && !qtyInput.getText().toString().isEmpty()) {
            qtyInput.setError("Quantity must be positive");
        } else {
            qtyInput.setError(null);
        }

        double total = qtyToSell * unitPrice;
        txtTotal.setText(String.format(Locale.US, "Total: ৳%.2f", total));

        updateGrandTotal();
        validateAllEntries();
    }

    private void updateGrandTotal() {
        double grandTotal = 0;
        for (int i = 0; i < entryContainer.getChildCount(); i++) {
            View entry = entryContainer.getChildAt(i);
            TextView txtTotal = entry.findViewById(R.id.txtTotalPrice);
            String val = txtTotal.getText().toString().replace("Total: ৳", "").trim();
            try {
                grandTotal += Double.parseDouble(val);
            } catch (Exception ignored) {}
        }
        grandTotalText.setText(String.format(Locale.US, "Grand Total: ৳%.2f", grandTotal));
    }

    private void validateAllEntries() {
        boolean allValid = true;

        for (int i = 0; i < entryContainer.getChildCount(); i++) {
            View entry = entryContainer.getChildAt(i);

            String batchNo = selectedBatchNos.get(i);
            EditText qtyInput = entry.findViewById(R.id.editQuantity);

            if (batchNo == null || !medicineMap.containsKey(batchNo)) {
                allValid = false;
                break;
            }

            String qtyStr = qtyInput.getText().toString().trim();
            if (qtyStr.isEmpty()) {
                allValid = false;
                break;
            }

            int qty;
            try {
                qty = Integer.parseInt(qtyStr);
            } catch (NumberFormatException e) {
                allValid = false;
                break;
            }

            DataSnapshot snap = medicineMap.get(batchNo);
            Integer stockObj = snap.child("quantity").getValue(Integer.class);
            int stock = stockObj != null ? stockObj : 0;

            if (qty <= 0 || qty > stock) {
                allValid = false;
                break;
            }
        }

        sellButton.setEnabled(allValid && entryContainer.getChildCount() > 0);
    }

    private void savePdfWithSAF(byte[] pdfBytes, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);

        this.pendingPdfBytes = pdfBytes;
        this.pendingFileName = fileName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null && pendingPdfBytes != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(data.getData())) {
                    outputStream.write(pendingPdfBytes);
                    Toast.makeText(this, "PDF saved successfully!", Toast.LENGTH_LONG).show();

                    pendingPdfBytes = null;
                    pendingFileName = null;

                    startActivity(new Intent(this, InventoryActivity.class));
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void processSale() {
        validateAllEntries();
        if (!sellButton.isEnabled()) {
            Toast.makeText(this, "Please fix errors before selling.", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateTime = sdf.format(new Date());

        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint();
        paint.setTextSize(12);
        paint.setAntiAlias(true);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setFakeBoldText(true);
        paint.setTextSize(16);
        canvas.drawText("MEDICINE SALES RECEIPT", 180, 50, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(12);

        canvas.drawText("Date: " + dateTime, 50, 80, paint);

        paint.setFakeBoldText(true);
        int y = 120;
        canvas.drawText("No.", 50, y, paint);
        canvas.drawText("Medicine", 100, y, paint);
        canvas.drawText("Batch", 250, y, paint);
        canvas.drawText("Avail.", 320, y, paint);
        canvas.drawText("Qty", 370, y, paint);
        canvas.drawText("Unit Price", 420, y, paint);
        canvas.drawText("Total", 500, y, paint);
        paint.setFakeBoldText(false);

        paint.setStrokeWidth(1f);
        canvas.drawLine(50, y + 5, 545, y + 5, paint);
        y += 25;

        double grandTotal = 0;
        int itemNumber = 1;

        for (int i = 0; i < entryContainer.getChildCount(); i++) {
            View entry = entryContainer.getChildAt(i);
            EditText qtyInput = entry.findViewById(R.id.editQuantity);

            String batchNo = selectedBatchNos.get(i);
            int qty = Integer.parseInt(qtyInput.getText().toString().trim());

            if (medicineMap.containsKey(batchNo)) {
                DataSnapshot snap = medicineMap.get(batchNo);
                String name = snap.child("name").getValue(String.class);
                String batchNoVal = snap.child("batchNo").getValue(String.class);
                Integer availableQty = snap.child("quantity").getValue(Integer.class);
                Double unitPrice = snap.child("price").getValue(Double.class);

                if (availableQty == null) availableQty = 0;
                if (unitPrice == null) unitPrice = 0.0;

                double total = qty * unitPrice;
                grandTotal += total;

                canvas.drawText(String.valueOf(itemNumber++), 50, y, paint);
                canvas.drawText(name, 100, y, paint);
                canvas.drawText(batchNoVal, 250, y, paint);
                canvas.drawText(String.valueOf(availableQty), 320, y, paint);
                canvas.drawText(String.valueOf(qty), 370, y, paint);
                canvas.drawText(String.format(Locale.US, "৳%.2f", unitPrice), 420, y, paint);
                canvas.drawText(String.format(Locale.US, "৳%.2f", total), 500, y, paint);
                y += 20;

                Map<String, Object> saleItem = new HashMap<>();
                saleItem.put("medicine", name);
                saleItem.put("batchNo", batchNoVal);
                saleItem.put("quantity", qty);
                saleItem.put("unitPrice", unitPrice);
                saleItem.put("total", total);
                salesRef.child(timestamp).push().setValue(saleItem);

                String key = snap.getKey();
                medicineRef.child(key).child("quantity").setValue(availableQty - qty);
            }
        }

        y += 20;
        paint.setStrokeWidth(1f);
        canvas.drawLine(50, y, 545, y, paint);
        y += 25;

        paint.setFakeBoldText(true);
        canvas.drawText("GRAND TOTAL:", 380, y, paint);
        canvas.drawText(String.format(Locale.US, "৳%.2f", grandTotal), 500, y, paint);
        paint.setFakeBoldText(false);
        y += 30;

        paint.setTextSize(10);
        canvas.drawText("Thank you for your purchase!", 220, y, paint);
        y += 20;
        //canvas.drawText("** Receipt - valid without signature **", 150, y, paint);

        pdf.finishPage(page);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            pdf.writeTo(outputStream);
            byte[] pdfBytes = outputStream.toByteArray();

            savePdfWithSAF(pdfBytes, "sale_" + timestamp + ".pdf");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pdf.close();
        }
    }

    @Override
    protected void onDestroy() {

        for (int i = 0; i < entryContainer.getChildCount(); i++) {
            View entry = entryContainer.getChildAt(i);
            if (entry != null) {
                EditText qtyInput = entry.findViewById(R.id.editQuantity);
                if (i < textWatchers.size() && qtyInput != null) {
                    qtyInput.removeTextChangedListener(textWatchers.get(i));
                }
            }
        }
        textWatchers.clear();
        super.onDestroy();
    }
}