package core;

import core.RoomType;
import system.DatabaseHandler;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Room {
    private int roomNumber;
    private RoomType roomType;
    private double pricePerNight;
    private boolean isAvailable;
    private String location;
    private String amenities;
    private DatabaseHandler dbHandler;

    public Room(int roomNumber, RoomType roomType, double pricePerNight, String location, String amenities, DatabaseHandler dbHandler) throws SQLException {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
        this.location = location;
        this.amenities = amenities;
        this.dbHandler = dbHandler;
        saveToDatabase();
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String getLocation() {
        return location;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
        updateInDatabase();
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
        updateInDatabase();
    }

    public void setLocation(String location) {
        this.location = location;
        updateInDatabase();
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
        updateInDatabase();
    }

    public void markOccupied() throws SQLException {
        this.isAvailable = false;
        updateInDatabase();
    }

    public void markAvailable() throws SQLException {
        this.isAvailable = true;
        updateInDatabase();
    }

    public boolean isAvailableForDates(Date checkInDate, Date checkOutDate) throws SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String checkIn = sdf.format(checkInDate);
        String checkOut = sdf.format(checkOutDate);
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                """
                SELECT 1 FROM bookings
                WHERE room_number = ? AND status NOT IN ('CANCELLED', 'COMPLETED')
                AND (
                    (check_in_date <= ? AND check_out_date >= ?) OR
                    (check_in_date <= ? AND check_out_date >= ?) OR
                    (check_in_date >= ? AND check_out_date <= ?)
                )
                LIMIT 1
                """)) {
            stmt.setInt(1, roomNumber);
            stmt.setString(2, checkOut);
            stmt.setString(3, checkIn);
            stmt.setString(4, checkOut);
            stmt.setString(5, checkIn);
            stmt.setString(6, checkIn);
            stmt.setString(7, checkOut);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        }
    }

    public String getRoomDetails() {
        return String.format("Room %d (%s, $%.2f/night)", roomNumber, roomType.getTypeName(), pricePerNight);
    }

    @Override
    public String toString() {
        return String.format("""
                Room Info:
                Room Number: %d
                Type: %s
                Price per Night: $%.2f
                Available: %b
                Location: %s
                Amenities: %s
                """, roomNumber, roomType.getTypeName(), pricePerNight, isAvailable, location, amenities);
    }

    private void saveToDatabase() throws SQLException {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO rooms (room_number, type_name, price_per_night, is_available, location, amenities) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, roomNumber);
            stmt.setString(2, roomType.getTypeName());
            stmt.setDouble(3, pricePerNight);
            stmt.setInt(4, isAvailable ? 1 : 0);
            stmt.setString(5, location);
            stmt.setString(6, amenities);
            stmt.executeUpdate();
        }
    }

    private void updateInDatabase() {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "UPDATE rooms SET type_name=?, price_per_night=?, is_available=?, location=?, amenities=? WHERE room_number=?")) {
            stmt.setString(1, roomType.getTypeName());
            stmt.setDouble(2, pricePerNight);
            stmt.setInt(3, isAvailable ? 1 : 0);
            stmt.setString(4, location);
            stmt.setString(5, amenities);
            stmt.setInt(6, roomNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update room in database: " + e.getMessage());
        }
    }
}
