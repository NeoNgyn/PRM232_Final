package com.example.final_project;

public class Meal {
    private String name;
    private int imageResource; // Dùng int để lưu ID của ảnh trong drawable
    private boolean isPro;

    public Meal(String name, int imageResource, boolean isPro) {
        this.name = name;
        this.imageResource = imageResource;
        this.isPro = isPro;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public boolean isPro() {
        return isPro;
    }
}
