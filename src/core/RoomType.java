
package core;

public class RoomType {
    private String typeName;
    private String description;
    private double basePrice;
    private int maxOccupancy;
    private boolean hasExtraBed;

    public RoomType(String typeName, String description, double basePrice, int maxOccupancy, boolean hasExtraBed) {
        this.typeName = typeName;
        this.description = description;
        this.basePrice = basePrice;
        this.maxOccupancy = maxOccupancy;
        this.hasExtraBed = hasExtraBed;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getDescription() {
        return description;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public boolean hasExtraBed() {
        return hasExtraBed;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public void setMaxOccupancy(int maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    public void setHasExtraBed(boolean hasExtraBed) {
        this.hasExtraBed = hasExtraBed;
    }

    @Override
    public String toString() {
        return String.format("""
                RoomType Info:
                Type Name: %s
                Description: %s
                Base Price: $%.2f
                Max Occupancy: %d
                Has Extra Bed: %b
                """, typeName, description, basePrice, maxOccupancy, hasExtraBed);
    }
}
