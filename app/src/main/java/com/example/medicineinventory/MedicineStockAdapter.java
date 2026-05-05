package com.example.medicineinventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicineStockAdapter extends RecyclerView.Adapter<MedicineStockAdapter.StockViewHolder> {

    private Context context;
    private List<Medicine> medicineList;

    public MedicineStockAdapter(Context context, List<Medicine> medicineList) {
        this.context = context;
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicine_stock, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);

        holder.textQuantityHeader.setText("Stock: " + medicine.getQuantity());
        holder.textMedicineName.setText(medicine.getName());
        holder.textBatchNo.setText(medicine.getBatchNo());
        holder.textManufacturer.setText(medicine.getManufacturer());
        holder.textCategory.setText(medicine.getCategory());
        holder.textPrice.setText(String.valueOf(medicine.getPrice()));
        holder.textExpiryDate.setText(medicine.getExpiryDate());
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView textQuantityHeader, textMedicineName, textBatchNo, textManufacturer,
                textCategory, textPrice, textExpiryDate;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            textQuantityHeader = itemView.findViewById(R.id.textQuantityHeaderStock);
            textMedicineName = itemView.findViewById(R.id.textMedicineNameStock);
            textBatchNo = itemView.findViewById(R.id.textBatchNoStock);
            textManufacturer = itemView.findViewById(R.id.textManufacturerStock);
            textCategory = itemView.findViewById(R.id.textCategoryStock);
            textPrice = itemView.findViewById(R.id.textPriceStock);
            textExpiryDate = itemView.findViewById(R.id.textExpiryDateStock);
        }
    }
}
