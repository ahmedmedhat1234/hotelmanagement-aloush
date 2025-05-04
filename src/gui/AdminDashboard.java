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

public class AdminDashboard {
    private JFrame frame;
    private User currentUser;
    private DatabaseHandler dbHandler;
    private BufferedImage backgroundImage;

    public AdminDashboard(User currentUser, DatabaseHandler dbHandler) {
        this.currentUser = currentUser;
        this.dbHandler = dbHandler;

        frame = new JFrame("Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        try {
            String imagePath = "/resources/photos/admin.png";
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

        JLabel welcomeLabel = new JLabel("Welcome, Admin " + currentUser.getUserName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setOpaque(true);
        welcomeLabel.setBackground(new Color(0, 51, 102));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonPanel.setOpaque(false);

        JButton manageUsersButton = new JButton("Manage Users");
        manageUsersButton.setFont(new Font("Arial", Font.BOLD, 16));
        manageUsersButton.addActionListener(e -> manageUsers());

        JButton manageRoomsButton = new JButton("Manage Rooms");
        manageRoomsButton.setFont(new Font("Arial", Font.BOLD, 16));
        manageRoomsButton.addActionListener(e -> manageRooms());

        JButton viewReportsButton = new JButton("View Reports");
        viewReportsButton.setFont(new Font("Arial", Font.BOLD, 16));
        viewReportsButton.addActionListener(e -> viewReports());

        buttonPanel.add(manageUsersButton);
        buttonPanel.add(manageRoomsButton);
        buttonPanel.add(viewReportsButton);

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

    private void manageUsers() {
        try {
            Admin admin = new Admin(currentUser.getId(), currentUser.getName(), currentUser.getUserName(),
                    currentUser.getPassword(), currentUser.getEmail(), 1, "Management", true, dbHandler);
            List<Customer> customers = admin.viewAllCustomers();
            String[] columns = {"ID", "Name", "Username", "Email"};
            Object[][] data = new Object[customers.size()][4];
            for (int i = 0; i < customers.size(); i++) {
                Customer c = customers.get(i);
                data[i][0] = c.getId();
                data[i][1] = c.getName();
                data[i][2] = c.getUserName();
                data[i][3] = c.getEmail();
            }
            JTable customerTable = new JTable(data, columns);
            JOptionPane.showMessageDialog(frame, new JScrollPane(customerTable), "All Customers", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error loading users: " + ex.getMessage());
        }
    }

    private void manageRooms() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField roomNumberField = new JTextField(10);
        JTextField typeNameField = new JTextField(10);
        JTextField priceField = new JTextField(10);
        panel.add(new JLabel("Room Number:"));
        panel.add(roomNumberField);
        panel.add(new JLabel("Room Type:"));
        panel.add(typeNameField);
        panel.add(new JLabel("Price per Night:"));
        panel.add(priceField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Room", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
                String typeName = typeNameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                RoomType roomType = new RoomType(typeName, "Standard", price, 2, false);
                Room room = new Room(roomNumber, roomType, price, "Main Building", "WiFi, TV", dbHandler);
                Admin admin = new Admin(currentUser.getId(), currentUser.getName(), currentUser.getUserName(),
                        currentUser.getPassword(), currentUser.getEmail(), 1, "Management", true, dbHandler);
                admin.addRoom(room);
                JOptionPane.showMessageDialog(frame, "Room added successfully!");
            } catch (NumberFormatException | SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error adding room: " + ex.getMessage());
            }
        }
    }

    private void viewReports() {
        try {
            Admin admin = new Admin(currentUser.getId(), currentUser.getName(), currentUser.getUserName(),
                    currentUser.getPassword(), currentUser.getEmail(), 1, "Management", true, dbHandler);
            List<Booking> bookings = admin.viewAllBookings();

            String[] columns = {"Booking ID", "Customer", "Room", "Check-In", "Check-Out", "Status", "Total Cost"};
            Object[][] data = new Object[bookings.size()][7];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (int i = 0; i < bookings.size(); i++) {
                Booking b = bookings.get(i);
                data[i][0] = b.getBookingID();
                data[i][1] = b.getCustomerId();
                data[i][2] = b.getRoomNumber();

                Object checkIn = b.getCheckInDate();
                Object checkOut = b.getCheckOutDate();

                // ✅ نطبع نوع الكائنات عشان نعرف بالضبط المشكلة فين
                System.out.println("checkIn type: " + (checkIn != null ? checkIn.getClass().getName() : "null"));
                System.out.println("checkOut type: " + (checkOut != null ? checkOut.getClass().getName() : "null"));

                // ✅ نحمي التنسيق من النوع الغلط
                try {
                    data[i][3] = (checkIn instanceof Date) ? sdf.format((Date) checkIn) : checkIn.toString();
                } catch (Exception e) {
                    data[i][3] = "Invalid Date";
                }

                try {
                    data[i][4] = (checkOut instanceof Date) ? sdf.format((Date) checkOut) : checkOut.toString();
                } catch (Exception e) {
                    data[i][4] = "Invalid Date";
                }

                data[i][5] = b.getStatus();
                data[i][6] = b.getTotalCost();
            }

            JTable bookingsTable = new JTable(data, columns);
            JOptionPane.showMessageDialog(frame, new JScrollPane(bookingsTable), "All Bookings", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "SQL Error loading reports: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Unexpected error: " + ex.getMessage());
        }
    }


    public void showForm() {
        frame.setVisible(true);
    }
}
