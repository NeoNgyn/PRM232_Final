package com.example.final_project.models.dto;

import java.util.List;

public class LogMealResponse {
    private List<Recognition> recognition_results;

    public List<Recognition> getRecognition_results() {
        return recognition_results;
    }

    public static class Recognition {
        private String name;
        private double probability;

        public String getName() {
            return name;
        }

        public double getProbability() {
            return probability;
        }
    }
}
