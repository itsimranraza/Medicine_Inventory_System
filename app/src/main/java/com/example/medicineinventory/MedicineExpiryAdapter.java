package com.example.medicineinventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicineExpiryAdapter extends RecyclerView.Adapter<MedicineExpiryAdapter.ExpiryViewHolder> {

    private Context context;
    private List<Medicine> medicineList;

    public MedicineExpiryAdapter(Context context, List<Medicine> medicineList) {
        this.context = context;
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public ExpiryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicine_expiry, parent, false);
        return new ExpiryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpiryViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);

        holder.textExpiryHeader.setText("Expiry: " + medicine.getExpiryDate());
        holder.textMedicineName.setText(medicine.getName());
        holder.textBatchNo.setText(medicine.getBatchNo());
        holder.textQuantity.setText(String.valueOf(medicine.getQuantity()));
        holder.textManufacturer.setText(medicine.getManufacturer());
        holder.textCategory.setText(medicine.getCategory());
        holder.textPrice.setText(String.valueOf(medicine.getPrice()));
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class ExpiryViewHolder extends RecyclerView.ViewHolder {
        TextView textExpiryHeader, textMedicineName, textBatchNo, textQuantity,
                textManufacturer, textCategory, textPrice;

        public ExpiryViewHolder(@NonNull View itemView) {
            super(itemView);
            textExpiryHeader = itemView.findViewById(R.id.textExpiryHeader);
            textMedicineName = itemView.findViewById(R.id.textMedicineNameExpiry);
            textBatchNo = itemView.findViewById(R.id.textBatchNoExpiry);
            textQuantity = itemView.findViewById(R.id.textQuantityExpiry);
            textManufacturer = itemView.findViewById(R.id.textManufacturerExpiry);
            textCategory = itemView.findViewById(R.id.textCategoryExpiry);
            textPrice = itemView.findViewById(R.id.textPriceExpiry);
        }
    }
}
