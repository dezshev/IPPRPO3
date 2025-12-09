package com.example;

import com.example.config.Config;
import com.example.service.ComparatorService;

public class Main {
    public static void main(String[] args) {
        Config config = new Config("app.properties");
        ComparatorService service = new ComparatorService(config);

        String mode = config.getMode(args);

        try {
            if ("scan".equalsIgnoreCase(mode)) {
                service.runScan();
            } else if ("diff".equalsIgnoreCase(mode)) {
                service.runDiff();
            } else {
                System.out.println("Unknown mode: " + mode);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}