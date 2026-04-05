package com.devtrails.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private int premium;   // weekly premium in ₹
    private int coverage;  // max payout in ₹

    private String tier;        // BASIC, STANDARD, PREMIUM
    private String description;
    private int aqiThreshold;      // trigger threshold
    private double rainfallThreshold; // mm/hr trigger

    public Plan() {}

    public Plan(String name, int premium, int coverage) {
        this.name = name;
        this.premium = premium;
        this.coverage = coverage;
        assignTierDefaults(name);
    }

    private void assignTierDefaults(String name) {
        switch (name) {
            case "Basic Shield" -> {
                this.tier = "BASIC";
                this.description = "For workers with less than 5 active delivery days. Covers AQI & rain disruptions.";
                this.aqiThreshold = 300;
                this.rainfallThreshold = 40.0;
            }
            case "Standard Shield" -> {
                this.tier = "STANDARD";
                this.description = "For regular active gig workers. Covers AQI & rain disruptions with higher payout.";
                this.aqiThreshold = 300;
                this.rainfallThreshold = 40.0;
            }
            case "Premium Shield" -> {
                this.tier = "PREMIUM";
                this.description = "For high-activity workers with 20+ deliveries/week. Maximum income protection.";
                this.aqiThreshold = 300;
                this.rainfallThreshold = 40.0;
            }
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getPremium() { return premium; }
    public int getCoverage() { return coverage; }
    public String getTier() { return tier; }
    public String getDescription() { return description; }
    public int getAqiThreshold() { return aqiThreshold; }
    public double getRainfallThreshold() { return rainfallThreshold; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPremium(int premium) { this.premium = premium; }
    public void setCoverage(int coverage) { this.coverage = coverage; }
    public void setTier(String tier) { this.tier = tier; }
    public void setDescription(String description) { this.description = description; }
    public void setAqiThreshold(int aqiThreshold) { this.aqiThreshold = aqiThreshold; }
    public void setRainfallThreshold(double rainfallThreshold) { this.rainfallThreshold = rainfallThreshold; }
}