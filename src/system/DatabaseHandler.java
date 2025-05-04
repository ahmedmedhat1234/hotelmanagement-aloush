 package system;

import core.Customer;
import core.Room;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:D:/hotelmanagement/hotel.db";

    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            System.out.println("Connected to database: " + DB_URL);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    public Class<Object> getClass(int customerId) {
        return null;
    }

    public Customer getcustomerid(int customerId) {
        return null;
    }

    public Room getRoomByNumber(int roomNumber) {
        return null;
    }

    public Customer getCustomerById(int customerId) {
        return null;
    }
}
