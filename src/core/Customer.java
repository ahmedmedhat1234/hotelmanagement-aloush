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

public class Customer extends User {
    private static final String ROLE = "CUSTOMER";
    private String nationalID;
    private String address;
    private String phoneNumber;
    private Date registrationDate;
    private DatabaseHandler dbHandler;

    public Customer(int id, String name, String userName, String password, String email,
                    String nationalID, String address, String phoneNumber, Date registrationDate, DatabaseHandler dbHandler) {
        super(id, name, userName, password, email, ROLE);
        this.nationalID = nationalID;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.registrationDate = registrationDate;
        this.dbHandler = dbHandler;
        saveToDatabase();
    }

    public String getNationalID() {
        return nationalID;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setNationalID(String nationalID) {
        this.nationalID = nationalID;
        updateInDatabase();
    }

    public void setAddress(String address) {
        this.address = address;
        updateInDatabase();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        updateInDatabase();
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
        updateInDatabase();
    }

    public Booking bookRoom(Room room, Date checkInDate, Date checkOutDate) throws SQLException, ParseException {
        if (room.isAvailableForDates(checkInDate, checkOutDate)) {
            try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement("SELECT MAX(booking_id) FROM bookings")) {
                try (var rs = stmt.executeQuery()) {
                    int newBookingID = rs.next() ? rs.getInt(1) + 1 : 1;
                    Booking booking = new Booking(newBookingID, this, room, checkInDate, checkOutDate,
                            BookingStatus.PENDING, 0.0, new Date(), dbHandler);
                    room.markOccupied();
                    return booking;
                }
            }
        } else {
            throw new IllegalStateException("Room is not available for the selected dates");
        }
    }

    public void cancelBooking(Booking booking) throws SQLException {
        booking.cancelBooking();
    }

    public List<Booking> viewMyBookings() throws SQLException, ParseException {
        List<Booking> bookings = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement("SELECT * FROM bookings WHERE customer_id = ?")) {
            stmt.setInt(1, getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int bookingID = rs.getInt("booking_id");
                    Room room = fetchRoom(rs.getInt("room_number"));
                    Date checkInDate = sdf.parse(rs.getString("check_in_date"));
                    Date checkOutDate = sdf.parse(rs.getString("check_out_date"));
                    BookingStatus status = BookingStatus.valueOf(rs.getString("status"));
                    double totalCost = rs.getDouble("total_cost");
                    Date bookingDate = sdf.parse(rs.getString("booking_date"));
                    bookings.add(new Booking(bookingID, this, room, checkInDate, checkOutDate, status, totalCost, bookingDate, dbHandler));
                }
            }
        }
        return bookings;
    }

    public void updateContactInfo(String phone, String address) {
        this.phoneNumber = phone;
        this.address = address;
        updateInDatabase();
    }

    public String requestInvoice(Booking booking) throws SQLException {
        return booking.generateInvoice();
    }

    @Override
    public String toString() {
        return String.format("""
                Customer Info:
                National ID: %s
                Address: %s
                Phone Number: %s
                Registration Date: %s
                """, nationalID, address, phoneNumber, registrationDate);
    }

    private void saveToDatabase() {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO users (id, name, username, password, email, role, national_id, address, phone_number, registration_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, getId());
            stmt.setString(2, getName());
            stmt.setString(3, getUserName());
            stmt.setString(4, getPassword());
            stmt.setString(5, getEmail());
            stmt.setString(6, ROLE);
            stmt.setString(7, nationalID);
            stmt.setString(8, address);
            stmt.setString(9, phoneNumber);
            stmt.setString(10, new SimpleDateFormat("yyyy-MM-dd").format(registrationDate));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save customer to database: " + e.getMessage());
        }
    }

    private void updateInDatabase() {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "UPDATE users SET name=?, email=?, national_id=?, address=?, phone_number=?, registration_date=? WHERE id=?")) {
            stmt.setString(1, getName());
            stmt.setString(2, getEmail());
            stmt.setString(3, nationalID);
            stmt.setString(4, address);
            stmt.setString(5, phoneNumber);
            stmt.setString(6, new SimpleDateFormat("yyyy-MM-dd").format(registrationDate));
            stmt.setInt(7, getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update customer in database: " + e.getMessage());
        }
    }

    private Room fetchRoom(int roomNumber) throws SQLException {
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement("SELECT * FROM rooms WHERE room_number = ?")) {
            stmt.setInt(1, roomNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RoomType roomType = new RoomType (rs.getString("type_name"), "", 0.0, 0, false);
                    return new Room (rs.getInt("room_number"), roomType, rs.getDouble("price_per_night"),
                            rs.getString("location"), rs.getString("amenities"), dbHandler);
                }
                throw new SQLException("Room not found: " + roomNumber);
            }
        }
    }

    public int getCustomerId() {
        return 0;
    }
}
