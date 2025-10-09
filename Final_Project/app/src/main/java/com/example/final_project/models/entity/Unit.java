package com.example.final_project.models.entity;

public class Unit {
    private int unitId;
    private String unitName;

    public Unit() {}

    public Unit(int unitId, String unitName) {
        this.unitId = unitId;
        this.unitName = unitName;
    }

    // Getter / Setter

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
}
