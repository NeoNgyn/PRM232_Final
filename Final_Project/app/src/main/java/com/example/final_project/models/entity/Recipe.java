package com.example.final_project.model;

public class Recipe {
    private String name;
    private String description;
    private String imageUrl;
    private String imageName1;
    private String imageName2;
    private String imageName3;
    private String note;

    public Recipe(String name, String description, String imageUrl, String imageName1, String imageName2, String imageName3, String note) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.imageName1 = imageName1;
        this.imageName2 = imageName2;
        this.imageName3 = imageName3;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageName1() {
        return imageName1;
    }

    public void setImageName1(String imageName1) {
        this.imageName1 = imageName1;
    }

    public String getImageName2() {
        return imageName2;
    }

    public void setImageName2(String imageName2) {
        this.imageName2 = imageName2;
    }

    public String getImageName3() {
        return imageName3;
    }

    public void setImageName3(String imageName3) {
        this.imageName3 = imageName3;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
