package gui;

import core.Booking;
import core.User;
import gui.LoginForm;
import payment.CardPayment;
import system.DatabaseHandler;

import javax.swing.*;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerDashboard {
    private JFrame frame;
    private User customer;
    private DatabaseHandler dbHandler;
    private JList<String> bookingList;
    private DefaultListModel<String> bookingListModel;

    public CustomerDashboard(User customer, DatabaseHandler dbHandler) {
        this.customer = customer;
        this.dbHandler = dbHandler;

        frame = new JFrame("Customer Dashboard - " + customer.getName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + customer.getName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        frame.add(welcomeLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        bookingListModel = new DefaultListModel<>();
        bookingList = new JList<>(bookingListModel);
        bookingList.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(new JScrollPane(bookingList), BorderLayout.CENTER);

        JButton bookRoomButton = new JButton("Book a Room");
        JButton payButton = new JButton("Pay for Selected Booking");
        bookRoomButton.setFont(new Font("Arial", Font.BOLD, 14));
        payButton.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(bookRoomButton);
        buttonPanel.add(payButton);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(centerPanel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        frame.add(logoutButton, BorderLayout.SOUTH);

        loadPendingBookings();

        bookRoomButton.addActionListener(e -> bookNewRoom());
        payButton.addActionListener(e -> {
            String selectedBooking = bookingList.getSelectedValue();
            if (selectedBooking == null) {
                JOptionPane.showMessageDialog(frame, "Please select a booking to pay for.");
                return;
            }
            int bookingId = extractBookingId(selectedBooking);
            payForBooking(bookingId);
        });

        logoutButton.addActionListener(e -> {
            frame.dispose();
            new LoginForm ().showForm();
        });
    }

    private void loadPendingBookings() {
        bookingListModel.clear();
        List<Booking> pendingBookings = new ArrayList<>();
        try {
            String query = "SELECT booking_id, customer_id, room_number, check_in_date, check_out_date, status, total_cost, booking_date " +
                    "FROM bookings WHERE customer_id = ? AND status = 'PENDING'";
            PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query);
            stmt.setInt(1, customer.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("room_number"),
                        rs.getString("check_in_date"),
                        rs.getString("check_out_date"),
                        rs.getString("status"),
                        rs.getDouble("total_cost"),
                        rs.getString("booking_date"),
                        dbHandler
                );
                pendingBookings.add(booking);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error loading bookings: " + ex.getMessage());
        }

        if (pendingBookings.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No pending bookings available.");
        } else {
            for (Booking b : pendingBookings) {
                String bookingInfo = "Booking ID: " + b.getBookingId() + " | Room: " + b.getRoomNumber() +
                        " | Check-in: " + b.getCheckInDate() + " | Total Cost: $" + b.getTotalCost();
                bookingListModel.addElement(bookingInfo);
            }
        }
    }

    private void bookNewRoom() {
        JTextField checkInDateField = new JTextField(10);
        JTextField checkOutDateField = new JTextField(10);
        JTextField roomNumberField = new JTextField(5);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Check-in Date (YYYY-MM-DD):"));
        panel.add(checkInDateField);
        panel.add(new JLabel("Check-out Date (YYYY-MM-DD):"));
        panel.add(checkOutDateField);
        panel.add(new JLabel("Room Number:"));
        panel.add(roomNumberField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Book a Room", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String checkInDate = checkInDateField.getText().trim();
                String checkOutDate = checkOutDateField.getText().trim();
                int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
                double totalCost = 100.0; // مثال، يمكن تعدله بناءً على سعر الغرفة

                Booking booking = new Booking(customer.getId(), roomNumber, checkInDate, checkOutDate, totalCost, dbHandler);
                JOptionPane.showMessageDialog(frame, "Room booked successfully! Booking ID: " + booking.getBookingId());
                loadPendingBookings();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid room number.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error booking room: " + e.getMessage());
            }
        }
    }

    private int extractBookingId(String bookingInfo) {
        String[] parts = bookingInfo.split(" \\| ");
        String idPart = parts[0]; // "Booking ID: 1"
        return Integer.parseInt(idPart.split(": ")[1]);
    }

    private void payForBooking(int bookingId) {
        JTextField cardNumberField = new JTextField(16);
        JTextField cardHolderField = new JTextField(20);
        JTextField expiryDateField = new JTextField(5);
        JTextField cvvField = new JTextField(3);

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Card Number:"));
        panel.add(cardNumberField);
        panel.add(new JLabel("Card Holder:"));
        panel.add(cardHolderField);
        panel.add(new JLabel("Expiry Date (MM/YY):"));
        panel.add(expiryDateField);
        panel.add(new JLabel("CVV:"));
        panel.add(cvvField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Enter Card Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                CardPayment payment = new CardPayment(
                        cardNumberField.getText(),
                        cardHolderField.getText(),
                        expiryDateField.getText(),
                        cvvField.getText(),
                        dbHandler,
                        bookingId
                );
                double totalCost = getBookingCost(bookingId);
                if (payment.processPayment(totalCost)) {
                    JOptionPane.showMessageDialog(frame, "Payment successful!");
                    loadPendingBookings();
                } else {
                    JOptionPane.showMessageDialog(frame, "Payment failed. Please check your card details.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error processing payment: " + ex.getMessage());
            }
        }
    }

    private double getBookingCost(int bookingId) throws SQLException {
        String query = "SELECT total_cost FROM bookings WHERE booking_id = ?";
        PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query);
        stmt.setInt(1, bookingId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getDouble("total_cost");
        }
        throw new SQLException("Booking not found: " + bookingId);
    }

    public void showForm() {
        frame.setVisible(true);
    }
}
