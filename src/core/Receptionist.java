 package core;

import core.*;
import system.DatabaseHandler;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Receptionist extends User {
    private static final String ROLE = "RECEPTIONIST";
    private int employeeID;
    private String branch;
    private String shift;
    private DatabaseHandler dbHandler;

    public Receptionist(int id, String name, String userName, String password, String email,
                        int employeeID, String branch, String shift, DatabaseHandler dbHandler) {
        super(id, name, userName, password, email, ROLE);
        this.employeeID = employeeID;
        this.branch = branch;
        this.shift = shift;
        this.dbHandler = dbHandler;
        saveToDatabase();
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public String getBranch() {
        return branch;
    }

    public String getShift() {
        return shift;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
        updateInDatabase();
    }

    public void setBranch(String branch) {
        this.branch = branch;
        updateInDatabase();
    }

    public void setShift(String shift) {
        this.shift = shift;
        updateInDatabase();
    }

    public void checkInCustomer(Booking booking) throws SQLException {
        if (false) {
            booking.setStatus(BookingStatus.CONFIRMED);
        } else {
            throw new IllegalStateException("Booking is not in PENDING status");
        }
    }

    public void checkOutCustomer(Booking booking) throws SQLException {
        if (false) {
            booking.setStatus(BookingStatus.COMPLETED);
            booking.getRoom().markAvailable();
        } else {
            throw new IllegalStateException("Booking is not in CONFIRMED status");
        }
    }

    public List<Booking> viewAllBookings() throws SQLException, ParseException {
        List<Booking> bookings = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement("SELECT * FROM bookings")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int bookingID = rs.getInt("booking_id");
                    int customerID = rs.getInt("customer_id");
                    int roomNumber = rs.getInt("room_number");
                    Date checkInDate = sdf.parse(rs.getString("check_in_date"));
                    Date checkOutDate = sdf.parse(rs.getString("check_out_date"));
                    BookingStatus status = BookingStatus.valueOf(rs.getString("status"));
                    double totalCost = rs.getDouble("total_cost");
                    Date bookingDate = sdf.parse(rs.getString("booking_date"));
                    Customer customer = fetchCustomer(customerID);
                    Room room = fetchRoom(roomNumber);
                    bookings.add(new Booking(bookingID, customer, room, checkInDate, checkOutDate, status, totalCost, bookingDate, dbHandler));
                }
            }
        }
        return bookings;
    }

    @Override
    public String toString() {
        return String.format("""
                Receptionist Info:
                Employee ID: %d
                Branch: %s
                Shift: %s
                """, employeeID, branch, shift);
    }

    private void saveToDatabase() {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO users (id, name, username, password, email, role, employee_id, branch, shift) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, getId());
            stmt.setString(2, getName());
            stmt.setString(3, getUserName());
            stmt.setString(4, getPassword());
            stmt.setString(5, getEmail());
            stmt.setString(6, ROLE);
            stmt.setInt(7, employeeID);
            stmt.setString(8, branch);
            stmt.setString(9, shift);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save receptionist to database: " + e.getMessage());
        }
    }

    private void updateInDatabase() {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "UPDATE users SET name=?, email=?, employee_id=?, branch=?, shift=? WHERE id=?")) {
            stmt.setString(1, getName());
            stmt.setString(2, getEmail());
            stmt.setInt(3, employeeID);
            stmt.setString(4, branch);
            stmt.setString(5, shift);
            stmt.setInt(6, getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update receptionist in database: " + e.getMessage());
        }
    }

    private Customer fetchCustomer(int customerId) throws SQLException {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement("SELECT * FROM users WHERE id = ? AND role = 'CUSTOMER'")) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(customerId, rs.getString("name"), rs.getString("username"),
                            rs.getString("password"), rs.getString("email"), rs.getString("national_id"),
                            rs.getString("address"), rs.getString("phone_number"),
                            new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("registration_date")), dbHandler);
                }
                throw new SQLException("Customer not found: " + customerId);
            }
        } catch (ParseException e) {
            throw new SQLException("Error parsing customer data: " + e.getMessage());
        }
    }

    private Room fetchRoom(int roomNumber) throws SQLException {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement("SELECT * FROM rooms WHERE room_number = ?")) {
            stmt.setInt(1, roomNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RoomType roomType = new RoomType(rs.getString("type_name"), "", rs.getDouble("price_per_night"), 0, false);
                    return new Room(rs.getInt("room_number"), roomType, rs.getDouble("price_per_night"),
                            rs.getString("location"), rs.getString("amenities"), dbHandler);
                }
                throw new SQLException("Room not found: " + roomNumber);
            }
        }
    }

    public List<Room> viewAvailableRooms() {
        return null;
    }
}
