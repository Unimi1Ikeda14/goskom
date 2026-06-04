package com.goskom.site.entities;

public enum BillType {
    COLD_WATER("💧", "Холодное водоснабжение", 1.55),   // 1.55 BYN за м³
    HOT_WATER("🔥", "Горячее водоснабжение", 2.50),    // 2.50 BYN за м³
    ELECTRICITY("⚡", "Электроэнергия", 0.25),         // 0.25 BYN за кВт/ч
    GAS("🔥💨", "Газоснабжение", 0.54),                // 0.54 BYN за м³
    HEATING("🌡️", "Отопление", 24.00);                 // 24.00 BYN за Гкал

    private final String icon;
    private final String displayName;
    private final double tariff; // Стоимость за единицу

    BillType(String icon, String displayName, double tariff) {
        this.icon = icon;
        this.displayName = displayName;
        this.tariff = tariff;
    }

    public String getIcon() { return icon; }
    public String getDisplayName() { return displayName; }
    public double getTariff() { return tariff; }
}