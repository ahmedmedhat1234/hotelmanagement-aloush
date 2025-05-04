package gui;

import core.User;
import system.DatabaseHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

public class BookingForm {
    private JFrame frame;
    private JComboBox<String> roomComboBox;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JLabel costLabel;
    private JButton calculateCostButton;
    private JButton confirmButton;
    private JLabel errorLabel;
    private User currentUser;
    private DatabaseHandler dbHandler;
    private double totalCost;
    private BufferedImage backgroundImage;

    public BookingForm(User currentUser, DatabaseHandler dbHandler) {
        this.currentUser = currentUser;
        this.dbHandler = dbHandler;
        this.totalCost = 0.0;

        frame = new JFrame("Book a Room");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        try {
            String imagePath = "/resources/photos/WhatsApp Image 2025-04-30 at 16.05.59_40ee9eba.jpg";
            backgroundImage = loadScaledImage(imagePath, 400, 300);
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            backgroundImage = null;
        }

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel roomLabel = new JLabel("Select Room:");
        roomLabel.setFont(new Font("Arial", Font.BOLD, 14));
        roomComboBox = new JComboBox<>(loadAvailableRooms());
        roomComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel checkInLabel = new JLabel("Check-In Date (yyyy-MM-dd):");
        checkInLabel.setFont(new Font("Arial", Font.BOLD, 14));
        checkInField = new JTextField(15);
        checkInField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel checkOutLabel = new JLabel("Check-Out Date (yyyy-MM-dd):");
        checkOutLabel.setFont(new Font("Arial", Font.BOLD, 14));
        checkOutField = new JTextField(15);
        checkOutField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel costTitleLabel = new JLabel("Total Cost:");
        costTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        costLabel = new JLabel("$0.00");
        costLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        calculateCostButton = new JButton("Calculate Cost");
        calculateCostButton.setFont(new Font("Arial", Font.BOLD, 14));
        calculateCostButton.setBackground(new Color(0, 153, 76));
        calculateCostButton.setForeground(Color.WHITE);

        confirmButton = new JButton("Confirm Booking");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 14));
        confirmButton.setBackground(new Color(0, 102, 204));
        confirmButton.setForeground(Color.WHITE);

        errorLabel = new JLabel("");
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(roomLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(roomComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(checkInLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(checkInField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(checkOutLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(checkOutField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(costTitleLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(costLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(calculateCostButton, gbc);

        gbc.gridy = 5;
        mainPanel.add(confirmButton, gbc);

        gbc.gridy = 6;
        mainPanel.add(errorLabel, gbc);

        backgroundPanel.add(mainPanel, BorderLayout.CENTER);
        frame.add(backgroundPanel, BorderLayout.CENTER);

        calculateCostButton.addActionListener(e -> calculateTotalCost());
        confirmButton.addActionListener(e -> confirmBooking());
    }

    private BufferedImage loadScaledImage(String path, int targetWidth, int targetHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(getClass().getResource(path));
        if (originalImage == null) {
            throw new IOException("Image resource not found: " + path);
        }
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        originalImage.flush();
        return scaledImage;
    }

    private String[] loadAvailableRooms() {
        List<String> rooms = new ArrayList<>();
        try {
            String query = "SELECT room_number, type_name, price_per_night FROM rooms WHERE is_available = 1";
            try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int roomNumber = rs.getInt("room_number");
                    String typeName = rs.getString("type_name");
                    double price = rs.getDouble("price_per_night");
                    rooms.add(String.format("Room %d (%s, $%.2f/night)", roomNumber, typeName, price));
                }
            }
        } catch (SQLException e) {
            errorLabel.setText("Error loading rooms: " + e.getMessage());
        }
        return rooms.toArray(new String[0]);
    }

    private void calculateTotalCost() {
        errorLabel.setText("");
        String roomSelection = (String) roomComboBox.getSelectedItem();
        String checkIn = checkInField.getText().trim();
        String checkOut = checkOutField.getText().trim();

        if (roomSelection == null || roomSelection.isEmpty()) {
            errorLabel.setText("Please select a room.");
            return;
        }
        if (checkIn.isEmpty() || checkOut.isEmpty()) {
            errorLabel.setText("Please enter check-in and check-out dates.");
            return;
        }

        int roomNumber;
        try {
            roomNumber = Integer.parseInt(roomSelection.split(" ")[1]);
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid room selection.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        Date checkInDate, checkOutDate;
        try {
            checkInDate = sdf.parse(checkIn);
            checkOutDate = sdf.parse(checkOut);
        } catch (ParseException e) {
            errorLabel.setText("Invalid date format. Use yyyy-MM-dd.");
            return;
        }

        if (!checkOutDate.after(checkInDate)) {
            errorLabel.setText("Check-out date must be after check-in date.");
            return;
        }

        if (!isRoomAvailable(roomNumber, checkIn, checkOut)) {
            errorLabel.setText("Selected room is not available for the specified dates.");
            return;
        }

        try {
            String query = "SELECT price_per_night FROM rooms WHERE room_number = ?";
            try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query)) {
                stmt.setInt(1, roomNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double pricePerNight = rs.getDouble("price_per_night");
                        long diffInMillies = checkOutDate.getTime() - checkInDate.getTime();
                        long nights = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                        if (nights <= 0) {
                            errorLabel.setText("Invalid date range.");
                            return;
                        }
                        totalCost = pricePerNight * nights;
                        costLabel.setText(String.format("$%.2f", totalCost));
                    } else {
                        errorLabel.setText("Room not found.");
                    }
                }
            }
        } catch (SQLException e) {
            errorLabel.setText("Error calculating cost: " + e.getMessage());
        }
    }

    private boolean isRoomAvailable(int roomNumber, String checkIn, String checkOut) {
        try {
            String query = """
                SELECT 1 FROM bookings
                WHERE room_number = ? AND status NOT IN ('CANCELLED', 'COMPLETED')
                AND (
                    (check_in_date <= ? AND check_out_date >= ?) OR
                    (check_in_date <= ? AND check_out_date >= ?) OR
                    (check_in_date >= ? AND check_out_date <= ?)
                )
                LIMIT 1
            """;
            try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query)) {
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
        } catch (SQLException e) {
            errorLabel.setText("Error checking room availability: " + e.getMessage());
            return false;
        }
    }

    private void confirmBooking() {
        errorLabel.setText("");
        String roomSelection = (String) roomComboBox.getSelectedItem();
        String checkIn = checkInField.getText().trim();
        String checkOut = checkOutField.getText().trim();

        if (roomSelection == null || roomSelection.isEmpty() || checkIn.isEmpty() || checkOut.isEmpty()) {
            errorLabel.setText("Please complete all fields.");
            return;
        }
        if (totalCost <= 0.0) {
            errorLabel.setText("Please calculate the cost first.");
            return;
        }

        int roomNumber;
        try {
            roomNumber = Integer.parseInt(roomSelection.split(" ")[1]);
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid room selection.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(checkIn);
            sdf.parse(checkOut);
        } catch (ParseException e) {
            errorLabel.setText("Invalid date format. Use yyyy-MM-dd.");
            return;
        }

        try {
            String query = """
                INSERT INTO bookings (customer_id, room_number, check_in_date, check_out_date, status, total_cost, booking_date)
                VALUES (?, ?, ?, ?, 'PENDING', ?, ?)
            """;
            try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query)) {
                stmt.setInt(1, currentUser.getId());
                stmt.setInt(2, roomNumber);
                stmt.setString(3, checkIn);
                stmt.setString(4, checkOut);
                stmt.setDouble(5, totalCost);
                stmt.setString(6, sdf.format(new Date()));
                stmt.executeUpdate();

                String updateRoomQuery = "UPDATE rooms SET is_available = 0 WHERE room_number = ?";
                try (PreparedStatement roomStmt = dbHandler.getConnection().prepareStatement(updateRoomQuery)) {
                    roomStmt.setInt(1, roomNumber);
                    roomStmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(frame, "Booking confirmed successfully!");
                frame.dispose();
            }
        } catch (SQLException e) {
            errorLabel.setText("Error saving booking: " + e.getMessage());
        }
    }

    public void showForm() {
        frame.setVisible(true);
    }
}
