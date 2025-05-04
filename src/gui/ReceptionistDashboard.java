package gui;

import core.*;
import system.DatabaseHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

public class ReceptionistDashboard {
    private JFrame frame;
    private User currentUser;
    private DatabaseHandler dbHandler;
    private BufferedImage backgroundImage;

    public ReceptionistDashboard(User currentUser, DatabaseHandler dbHandler) {
        this.currentUser = currentUser;
        this.dbHandler = dbHandler;

        frame = new JFrame("Receptionist Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        try {
            String imagePath = "/resources/photos/receptionist_background.jpg";
            backgroundImage = loadScaledImage(imagePath, 800, 600);
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

        JLabel welcomeLabel = new JLabel("Welcome, Receptionist " + currentUser.getUserName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setOpaque(true);
        welcomeLabel.setBackground(new Color(0, 51, 102));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonPanel.setOpaque(false);

        JButton checkInButton = new JButton("Check-In Guest");
        checkInButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkInButton.addActionListener(e -> checkInGuest());

        JButton checkOutButton = new JButton("Check-Out Guest");
        checkOutButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkOutButton.addActionListener(e -> checkOutGuest());

        JButton viewBookingsButton = new JButton("View Bookings");
        viewBookingsButton.setFont(new Font("Arial", Font.BOLD, 16));
        viewBookingsButton.addActionListener(e -> viewBookings());

        buttonPanel.add(checkInButton);
        buttonPanel.add(checkOutButton);
        buttonPanel.add(viewBookingsButton);

        backgroundPanel.add(welcomeLabel, BorderLayout.NORTH);
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);
        frame.add(backgroundPanel, BorderLayout.CENTER);
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

    private void checkInGuest() {
        try {
            Receptionist receptionist = new Receptionist(
                    currentUser.getId(),
                    currentUser.getName(),
                    currentUser.getUserName(),
                    currentUser.getPassword(),
                    currentUser.getEmail(),
                    1,
                    "Main Branch",
                    "Day",
                    dbHandler
            );

            List<Booking> bookings = receptionist.viewAllBookings();
            List<Booking> pendingBookings = bookings.stream()
                    .filter(b -> b.getStatus() != null && b.getStatus().equals("PENDING"))
                    .toList();

            if (pendingBookings.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No pending bookings available.");
                return;
            }

            String[] bookingOptions = pendingBookings.stream()
                    .map(b -> "Booking ID: " + b.getBookingID() + " (Customer: " + b.getCustomerId() + ")")
                    .toArray(String[]::new);

            JComboBox<String> bookingComboBox = new JComboBox<>(bookingOptions);
            JPanel panel = new JPanel();
            panel.add(new JLabel("Select Booking:"));
            panel.add(bookingComboBox);

            int result = JOptionPane.showConfirmDialog(frame, panel, "Check-In Guest", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            int bookingIndex = bookingComboBox.getSelectedIndex();
            Booking selectedBooking = pendingBookings.get(bookingIndex);

            List<Room> availableRooms = receptionist.viewAvailableRooms();
            if (availableRooms.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No available rooms for check-in.");
                return;
            }

            String[] roomOptions = availableRooms.stream()
                    .map(Room::getRoomDetails)
                    .toArray(String[]::new);

            JComboBox<String> roomComboBox = new JComboBox<>(roomOptions);
            JPanel roomPanel = new JPanel();
            roomPanel.add(new JLabel("Select Room:"));
            roomPanel.add(roomComboBox);

            int roomResult = JOptionPane.showConfirmDialog(frame, roomPanel, "Assign Room", JOptionPane.OK_CANCEL_OPTION);
            if (roomResult != JOptionPane.OK_OPTION) return;

            Room selectedRoom = availableRooms.get(roomComboBox.getSelectedIndex());

            // تحديث رقم الغرفة في الحجز
            selectedBooking.setroomnumber(selectedRoom.getRoomNumber());

            receptionist.checkInCustomer(selectedBooking);
            JOptionPane.showMessageDialog(frame, "Check-in successful for Booking ID: " + selectedBooking.getBookingID());

        } catch (SQLException | ParseException ex) {
            JOptionPane.showMessageDialog(frame, "Error checking in guest: " + ex.getMessage());
        }
    }

    private void checkOutGuest() {
        try {
            Receptionist receptionist = new Receptionist(
                    currentUser.getId(), currentUser.getName(), currentUser.getUserName(),
                    currentUser.getPassword(), currentUser.getEmail(), 1,
                    "Main Branch", "Day", dbHandler
            );

            List<Booking> bookings = receptionist.viewAllBookings();
            List<Booking> confirmedBookings = bookings.stream()
                    .filter(b -> b.getStatus() != null && b.getStatus().equals("CONFIRMED"))
                    .toList();

            if (confirmedBookings.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No confirmed bookings available.");
                return;
            }

            String[] bookingOptions = confirmedBookings.stream()
                    .map(b -> {
                        return "Booking ID: " + b.getBookingID() + " (Customer: " +
                                dbHandler.getClass (b.getCustomerId()).getName() + ")";
                    })
                    .toArray(String[]::new);

            JComboBox<String> bookingComboBox = new JComboBox<>(bookingOptions);
            JPanel panel = new JPanel();
            panel.add(new JLabel("Select Booking:"));
            panel.add(bookingComboBox);

            int result = JOptionPane.showConfirmDialog(frame, panel, "Check-Out Guest", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return;

            Booking selectedBooking = confirmedBookings.get(bookingComboBox.getSelectedIndex());
            receptionist.checkOutCustomer(selectedBooking);
            JOptionPane.showMessageDialog(frame, "Check-out successful for Booking ID: " + selectedBooking.getBookingID());

        } catch (SQLException | ParseException ex) {
            JOptionPane.showMessageDialog(frame, "Error checking out guest: " + ex.getMessage());
        }
    }

    private void viewBookings() {
        try {
            Receptionist receptionist = new Receptionist(
                    currentUser.getId(), currentUser.getName(), currentUser.getUserName(),
                    currentUser.getPassword(), currentUser.getEmail(), 1,
                    "Main Branch", "Day", dbHandler
            );

            List<Booking> bookings = receptionist.viewAllBookings();
            String[] columns = {"Booking ID", "Customer", "Room", "Check-In", "Check-Out", "Status", "Total Cost"};
            Object[][] data = new Object[bookings.size()][7];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (int i = 0; i < bookings.size(); i++) {
                Booking b = bookings.get(i);
                Customer customer = dbHandler.getCustomerById(b.getCustomerId());
                Room room = dbHandler.getRoomByNumber(b.getRoomNumber());

                Object checkInObj = b.getCheckInDate();
                Object checkOutObj = b.getCheckOutDate();

                data[i][0] = b.getBookingID();
                data[i][1] = (customer != null) ? customer.getName() : "Unknown";
                data[i][2] = (room != null) ? room.getRoomDetails() : "Unknown";
                data[i][3] = (checkInObj instanceof Date) ? sdf.format((Date) checkInObj) : "N/A";
                data[i][4] = (checkOutObj instanceof Date) ? sdf.format((Date) checkOutObj) : "N/A";
                data[i][5] = b.getStatus();
                data[i][6] = b.getTotalCost();
            }


            JTable bookingsTable = new JTable(data, columns);
            JOptionPane.showMessageDialog(frame, new JScrollPane(bookingsTable), "All Bookings", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException | ParseException ex) {
            JOptionPane.showMessageDialog(frame, "Error loading bookings: " + ex.getMessage());
        }
    }

    public void showForm() {
        frame.setVisible(true);
    }
}
