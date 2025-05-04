package payment;

import system.DatabaseHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CashPayment implements Payment {
    private String cardNumber;
    private String cardHolder;
    private String expiryDate;
    private String cvv;
    private DatabaseHandler dbHandler;
    private int bookingId;

    public void CardPayment(String cardNumber, String cardHolder, String expiryDate, String cvv, DatabaseHandler dbHandler, int bookingId) {
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.dbHandler = dbHandler;
        this.bookingId = bookingId;
    }

    @Override
    public boolean processPayment(double amount) {
        if (!validateCardDetails()) {
            return false;
        }
        try {
            PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                    "UPDATE bookings SET status = 'CONFIRMED' WHERE booking_id = ?");
            stmt.setInt(1, bookingId);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating booking status: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean processRefund(double amount) {
        return false;
    }

    @Override
    public PaymentStatus getPaymentStatus() {
        return null;
    }

    private boolean validateCardDetails() {
        return cardNumber.length() == 16 && cvv.length() == 3 && expiryDate.matches("\\d{2}/\\d{2}");
    }
}
