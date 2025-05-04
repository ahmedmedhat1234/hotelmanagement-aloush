package gui;

import core.User;
import gui.AdminDashboard;
import gui.CustomerDashboard;
import gui.ReceptionistDashboard;
import system.DatabaseHandler;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class LoginForm {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;
    private BufferedImage backgroundImage;
    private DatabaseHandler dbHandler;

    public LoginForm() {
        dbHandler = new DatabaseHandler();
        try {
            dbHandler.connect();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            System.exit(1);
        }

        frame = new JFrame("Hotel Management System - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        try {
            String imagePath = "/resources/photos/photo-1566073771259-6a8506099945.jpeg";
            backgroundImage = loadScaledImage(imagePath, 500, 600);
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
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 102, 204), 0, getHeight(), new Color(0, 51, 102));
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome to ANU Hotel", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 36));
        welcomeLabel.setForeground(new Color(255, 215, 0));
        welcomeLabel.setOpaque(true);
        welcomeLabel.setBackground(new Color(11, 11, 125));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        usernameLabel.setForeground(Color.BLUE);
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBorder(createRoundedBorder());

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 16));
        passwordLabel.setForeground(Color.BLUE);
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBorder(createRoundedBorder());

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setBackground(new Color(0, 153, 76));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(createRoundedBorder());

        errorLabel = new JLabel("");
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);

        gbc.gridy = 3;
        loginPanel.add(errorLabel, gbc);

        backgroundPanel.add(welcomeLabel, BorderLayout.NORTH);
        backgroundPanel.add(loginPanel, BorderLayout.CENTER);

        frame.add(backgroundPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> {
            User user = validateCredentials();
            if (user != null) {
                errorLabel.setText("Login successful!");
                redirectToDashboard(user);
            }
        });
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

    private Border createRoundedBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 204), 2, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }

    private boolean isValidUsername(String username) {
        return username != null && username.length() >= 4 && Pattern.matches("^[A-Za-z0-9_]+$", username);
    }

    private User validateCredentials() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        System.out.println("Attempting login with username: " + username);

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            System.out.println("Username or password is empty");
            return null;
        }

        if (!isValidUsername(username)) {
            errorLabel.setText("Username must be at least 4 characters and contain only letters, numbers, or underscores.");
            System.out.println("Invalid username format");
            return null;
        }

        try {
            String query = "SELECT id, name, email, password, role FROM users WHERE username = ?";
            System.out.println("Executing query: " + query + " with username: " + username);
            try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(query)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("id");
                        String name = rs.getString("name");
                        String email = rs.getString("email");
                        String storedPassword = rs.getString("password");
                        String role = rs.getString("role");

                        System.out.println("User found: ID=" + userId + ", Name=" + name + ", Email=" + email + ", Role=" + role);
                        System.out.println("Stored password: " + storedPassword);
                        System.out.println("Input password: " + password);

                        if (role == null || role.trim().isEmpty()) {
                            errorLabel.setText("User role is missing in database.");
                            System.out.println("Role is null or empty");
                            return null;
                        }

                        if (storedPassword.equals(password)) {
                            System.out.println("Password match! Login successful");
                            return new User(userId, name, username, password, email, role) {
                                @Override
                                public String getRole() {
                                    return role;
                                }
                            };
                        } else {
                            errorLabel.setText("Incorrect password.");
                            System.out.println("Password mismatch");
                        }
                    } else {
                        errorLabel.setText("Username not found.");
                        System.out.println("No user found with username: " + username);
                    }
                }
            }
        } catch (SQLException ex) {
            errorLabel.setText("Database error: " + ex.getMessage());
            System.err.println("SQL Exception: " + ex.getMessage());
        }
        return null;
    }

    private void redirectToDashboard(User user) {
        frame.dispose();
        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            errorLabel.setText("User role is missing or invalid.");
            return;
        }
        switch (role.toUpperCase()) {
            case "ADMIN":
                new AdminDashboard (user, dbHandler).showForm();
                break;
            case "CUSTOMER":
                new CustomerDashboard (user, dbHandler).showForm();
                break;
            case "RECEPTIONIST":
                new ReceptionistDashboard (user, dbHandler).showForm();
                break;
            default:
                errorLabel.setText("Invalid user role: " + role);
                break;
        }
    }

    public void showForm() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().showForm());
    }
}
