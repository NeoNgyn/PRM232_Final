package com.example.final_project.models.entity;

import java.util.Date;

public class ScanHistory {
    private String scanId;
    private String result;
    private String imageUrl;
    private Date createdAt;

    // Quan hệ N - 1
    private User user;

    public ScanHistory() {}

    public ScanHistory(String scanId, String result, String imageUrl, Date createdAt, User user) {
        this.scanId = scanId;
        this.result = result;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.user = user;
    }

    // Getter / Setter

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}