package payment;

import system.DatabaseHandler;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CardPayment {
    private String cardNumber;
    private String cardHolder;
    private String expiryDate;
    private String cvv;
    private DatabaseHandler dbHandler;
    private int bookingId;

    public CardPayment(String cardNumber, String cardHolder, String expiryDate, String cvv, DatabaseHandler dbHandler, int bookingId) {
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.dbHandler = dbHandler;
        this.bookingId = bookingId;
    }

    public boolean processPayment(double amount) {
        if (!validateCardDetails()) {
            return false;
        }

        try {
            // Check if the booking exists and get its total cost
            String query = "SELECT total_cost, status FROM bookings WHERE booking_id = ?";
            try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query)) {
                stmt.setInt(1, bookingId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double bookingCost = rs.getDouble("total_cost");
                        String status = rs.getString("status");

                        if (!status.equals("PENDING")) {
                            System.err.println("Booking is not in PENDING status");
                            return false;
                        }

                        if (amount < bookingCost) {
                            System.err.println("Insufficient payment amount");
                            return false;
                        }

                        // Update booking status to CONFIRMED
                        String updateQuery = "UPDATE bookings SET status = 'CONFIRMED', booking_date = ? WHERE booking_id = ?";
                        try (PreparedStatement updateStmt = dbHandler.getConnection().prepareStatement(updateQuery)) {
                            updateStmt.setString(1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                            updateStmt.setInt(2, bookingId);
                            updateStmt.executeUpdate();
                        }

                        // Log the payment (assuming a payments table exists; adjust as needed)
                        String paymentQuery = "INSERT INTO payments (booking_id, amount, payment_method, payment_date) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement paymentStmt = dbHandler.getConnection().prepareStatement(paymentQuery)) {
                            paymentStmt.setInt(1, bookingId);
                            paymentStmt.setDouble(2, amount);
                            paymentStmt.setString(3, "CARD");
                            paymentStmt.setString(4, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                            paymentStmt.executeUpdate();
                        }

                        return true;
                    } else {
                        System.err.println("Booking not found: " + bookingId);
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error processing payment: " + e.getMessage());
            return false;
        }
    }

    private boolean validateCardDetails() {
        // Basic card validation (for demo purposes)
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            System.err.println("Invalid card number");
            return false;
        }
        if (cardHolder == null || cardHolder.trim().isEmpty()) {
            System.err.println("Card holder name is required");
            return false;
        }
        if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            System.err.println("Invalid expiry date format (MM/YY)");
            return false;
        }
        if (cvv == null || !cvv.matches("\\d{3}")) {
            System.err.println("Invalid CVV");
            return false;
        }
        return true;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCvv() {
        return cvv;
    }
}
