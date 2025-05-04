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

public class Admin extends User {
    private static final String ROLE = "ADMIN";
    private int adminLevel;
    private String department;
    private boolean isSuperAdmin;
    private DatabaseHandler dbHandler;

    public Admin(int id, String name, String userName, String password, String email,
                 int adminLevel, String department, boolean isSuperAdmin, DatabaseHandler dbHandler) {
        super(id, name, userName, password, email, ROLE);
        this.adminLevel = adminLevel;
        this.department = department;
        this.isSuperAdmin = isSuperAdmin;
        this.dbHandler = dbHandler;
        saveToDatabase();
    }

    public int getAdminLevel() {
        return adminLevel;
    }

    public String getDepartment() {
        return department;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setAdminLevel(int adminLevel) {
        this.adminLevel = adminLevel;
        updateInDatabase();
    }

    public void setDepartment(String department) {
        this.department = department;
        updateInDatabase();
    }

    public void setSuperAdmin(boolean isSuperAdmin) {
        this.isSuperAdmin = isSuperAdmin;
        updateInDatabase();
    }

    public void addRoom(Room room) throws SQLException {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "INSERT INTO rooms (room_number, type_name, price_per_night, is_available, location, amenities) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType().getTypeName());
            stmt.setDouble(3, room.getPricePerNight());
            stmt.setInt(4, room.isAvailable() ? 1 : 0);
            stmt.setString(5, room.getLocation());
            stmt.setString(6, room.getAmenities());
            stmt.executeUpdate();
        }
    }

    public void updateRoom(Room room) throws SQLException {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "UPDATE rooms SET type_name=?, price_per_night=?, is_available=?, location=?, amenities=? WHERE room_number=?")) {
            stmt.setString(1, room.getRoomType().getTypeName());
            stmt.setDouble(2, room.getPricePerNight());
            stmt.setInt(3, room.isAvailable() ? 1 : 0);
            stmt.setString(4, room.getLocation());
            stmt.setString(5, room.getAmenities());
            stmt.setInt(6, room.getRoomNumber());
            stmt.executeUpdate();
        }
    }

    public void deleteRoom(int roomNumber) throws SQLException {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement("DELETE FROM rooms WHERE room_number = ?")) {
            stmt.setInt(1, roomNumber);
            stmt.executeUpdate();
        }
    }

    public List<Customer> viewAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement("SELECT * FROM users WHERE role = 'CUSTOMER'")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    String email = rs.getString("email");
                    String nationalID = rs.getString("national_id");
                    String address = rs.getString("address");
                    String phoneNumber = rs.getString("phone_number");
                    Date registrationDate = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("registration_date"));
                    customers.add(new Customer(id, name, username, password, email, nationalID, address, phoneNumber, registrationDate, dbHandler));
                }
            } catch (ParseException e) {
                throw new SQLException("Error parsing registration date: " + e.getMessage());
            }
        }
        return customers;
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

    public void approveBooking(Booking booking) throws SQLException {
        booking.confirmBooking();
    }

    public void cancelBooking(Booking booking) throws SQLException {
        booking.cancelBooking();
    }

    @Override
    public String toString() {
        return String.format("""
                Admin Info:
                Admin Level: %d
                Department: %s
                Super Admin: %b
                """, adminLevel, department, isSuperAdmin);
    }

    private void saveToDatabase() {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO users (id, name, username, password, email, role, admin_level, department, is_super_admin) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, getId());
            stmt.setString(2, getName());
            stmt.setString(3, getUserName());
            stmt.setString(4, getPassword());
            stmt.setString(5, getEmail());
            stmt.setString(6, ROLE);
            stmt.setInt(7, adminLevel);
            stmt.setString(8, department);
            stmt.setInt(9, isSuperAdmin ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save admin to database: " + e.getMessage());
        }
    }

    private void updateInDatabase() {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "UPDATE users SET name=?, email=?, admin_level=?, department=?, is_super_admin=? WHERE id=?")) {
            stmt.setString(1, getName());
            stmt.setString(2, getEmail());
            stmt.setInt(3, adminLevel);
            stmt.setString(4, department);
            stmt.setInt(5, isSuperAdmin ? 1 : 0);
            stmt.setInt(6, getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update admin in database: " + e.getMessage());
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
                    RoomType roomType = new RoomType (rs.getString("type_name"), "", rs.getDouble("price_per_night"), 0, false);
                    return new Room(rs.getInt("room_number"), roomType, rs.getDouble("price_per_night"),
                            rs.getString("location"), rs.getString("amenities"), dbHandler);
                }
                throw new SQLException("Room not found: " + roomNumber);
            }
        }
    }
}
