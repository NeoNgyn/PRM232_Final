package com.example.assignment_task1.model;

import java.util.Date;

public class Food {
    private String name;
    private String imageResourceName; // tên resource drawable, ví dụ: "egg"
    private int quantity;
    private Date expiryDate;
    private String note;
    private Date addedDate;
    private String category;

    public Food(String name, String imageResourceName, int quantity, Date expiryDate, String note, Date addedDate, String category) {
        this.name = name;
        this.imageResourceName = imageResourceName;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.note = note;
        this.addedDate = addedDate;
        this.category = category;
    }

    public String getName() { return name; }
    public String getImageResourceName() { return imageResourceName; }
    public int getQuantity() { return quantity; }
    public Date getExpiryDate() { return expiryDate; }
    public String getNote() { return note; }
    public Date getAddedDate() { return addedDate; }
    public String getCategory() { return category; }

    public void setName(String name) { this.name = name; }
    public void setImageResourceName(String imageResourceName) { this.imageResourceName = imageResourceName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public void setNote(String note) { this.note = note; }
    public void setAddedDate(Date addedDate) { this.addedDate = addedDate; }
    public void setCategory(String category) { this.category = category; }
}
