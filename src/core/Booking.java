package core;

import system.DatabaseHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Booking {
    private int bookingId;
    private int customerId;
    private int roomNumber;
    private String checkInDate;
    private String checkOutDate;
    private String status;
    private double totalCost;
    private String bookingDate;
    private DatabaseHandler dbHandler;

    public Booking(int customerId, int roomNumber, String checkInDate, String checkOutDate, double totalCost, DatabaseHandler dbHandler) throws SQLException {
        this.customerId = customerId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = "PENDING";
        this.totalCost = totalCost;
        this.bookingDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.dbHandler = dbHandler;
        saveToDatabase();
    }

    public Booking(int bookingId, int customerId, int roomNumber, String checkInDate, String checkOutDate, String status, double totalCost, String bookingDate, DatabaseHandler dbHandler) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.totalCost = totalCost;
        this.bookingDate = bookingDate;
        this.dbHandler = dbHandler;
    }

    public Booking(int bookingID, Customer customer, Room room, Date checkInDate, Date checkOutDate, BookingStatus status, double totalCost, Date bookingDate, DatabaseHandler dbHandler) {

    }

    private void saveToDatabase() throws SQLException {
        PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "INSERT INTO bookings (customer_id, room_number, check_in_date, check_out_date, status, total_cost, booking_date) VALUES (?, ?, ?, ?, ?, ?, ?)",
                java.sql.Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, customerId);
        stmt.setInt(2, roomNumber);
        stmt.setString(3, checkInDate);
        stmt.setString(4, checkOutDate);
        stmt.setString(5, status);
        stmt.setDouble(6, totalCost);
        stmt.setString(7, bookingDate);
        stmt.executeUpdate();
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            this.bookingId = generatedKeys.getInt(1);
            System.out.println("Generated booking ID: " + bookingId);
        } else {
            throw new SQLException("Failed to retrieve generated booking ID");
        }
    }

    public void updateStatus(String newStatus) throws SQLException {
        this.status = newStatus;
        PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "UPDATE bookings SET status = ? WHERE booking_id = ?");
        stmt.setString(1, status);
        stmt.setInt(2, bookingId);
        stmt.executeUpdate();
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public Object getBookingID() {
        return null;
    }

    public void cancelBooking() {

    }

    public void setStatus(BookingStatus bookingStatus) {

    }

    public Room getRoom() {
        return null;
    }

    public String generateInvoice() {
        return null;
    }

    public void confirmBooking() {

    }

    public Class<Object> getCustomer() {
        return null;
    }

    public void setroomnumber(int roomNumber) {
    }
}
