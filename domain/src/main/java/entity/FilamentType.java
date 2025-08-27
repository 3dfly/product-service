package entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
public enum FilamentType {
    PLA("Polylactic Acid", 190, 220, 50, 70, "Easy to print, biodegradable"),
    ABS("Acrylonitrile Butadiene Styrene", 220, 250, 80, 110, "Strong, durable, requires heated bed"),
    PETG("Polyethylene Terephthalate Glycol", 220, 250, 70, 90, "Chemical resistant, clear, strong"),
    TPU("Thermoplastic Polyurethane", 200, 230, 40, 60, "Flexible, rubber-like"),
    ASA("Acrylonitrile Styrene Acrylate", 240, 260, 90, 110, "UV resistant, outdoor use"),
    PC("Polycarbonate", 270, 310, 90, 120, "Very strong, high temperature resistance"),
    NYLON("Nylon", 240, 270, 60, 90, "Strong, flexible, wear resistant"),
    WOOD("Wood Filled", 180, 220, 50, 70, "Wood-like appearance, can be sanded"),
    METAL("Metal Filled", 190, 220, 50, 70, "Heavy, metallic appearance"),
    CARBON_FIBER("Carbon Fiber", 220, 250, 70, 90, "Lightweight, very strong"),
    HIPS("High Impact Polystyrene", 220, 240, 90, 110, "Good support material, can be dissolved"),
    PVA("Polyvinyl Alcohol", 180, 220, 45, 60, "Water soluble, support material");

    private final String fullName;
    private final int minTempC;
    private final int maxTempC;
    private final int minBedTempC;
    private final int maxBedTempC;
    private final String description;

    FilamentType(String fullName, int minTempC, int maxTempC, int minBedTempC, int maxBedTempC, String description) {
        this.fullName = fullName;
        this.minTempC = minTempC;
        this.maxTempC = maxTempC;
        this.minBedTempC = minBedTempC;
        this.maxBedTempC = maxBedTempC;
        this.description = description;
    }
}
