package com.example.medicineinventory;

public class Medicine {

    private String name;
    private String batchNo;
    private String manufacturer;
    private int quantity;
    private String expiryDate;
    private double price;
    private String category;
    
    public Medicine() {
    }

    public Medicine(String name, String batchNo, String manufacturer, int quantity,
                    String expiryDate, double price, String category) {
        this.name = name;
        this.batchNo = batchNo;
        this.manufacturer = manufacturer;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.price = price;
        this.category = category;
    }


    public String getName() {
        return name;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
