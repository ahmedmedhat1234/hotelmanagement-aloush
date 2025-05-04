package core;

import system.DatabaseHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

public abstract class User {
    private final int id;
    private String name;
    private String userName;
    private String password;
    private String email;
    private final String role;
    private DatabaseHandler dbHandler;

    public User(int id, String name, String userName, String password, String email, String role) {
        if (!isValidUsername(userName)) {
            throw new IllegalArgumentException("Invalid username");
        }
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public boolean isValidUsername(String username) {
        return username != null && username.length() >= 4 && Pattern.matches("^[A-Za-z0-9_]+$", username);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public void setName(String name) {
        this.name = name;
        updateInDatabase();
    }

    public void setUserName(String userName) {
        if (!isValidUsername(userName)) {
            throw new IllegalArgumentException("Invalid username");
        }
        this.userName = userName;
        updateInDatabase();
    }

    public void setPassword(String password) {
        this.password = password;
        updateInDatabase();
    }

    public void setEmail(String email) {
        this.email = email;
        updateInDatabase();
    }

    public void setDbHandler(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    private void updateInDatabase() {
        if (dbHandler == null) return;
        try (PreparedStatement stmt = dbHandler.getConnection().prepareStatement(
                "UPDATE users SET name = ?, username = ?, password = ?, email = ? WHERE id = ?")) {
            stmt.setString(1, name);
            stmt.setString(2, userName);
            stmt.setString(3, password);
            stmt.setString(4, email);
            stmt.setInt(5, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update user in database: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return String.format("User: %s, Role: %s, Email: %s", name, role, email);
    }
}
