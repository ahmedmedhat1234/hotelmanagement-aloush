package system;

import java.sql.SQLException;

public class HotelSystem {
    private DatabaseHandler dbHandler;
    private String hotelName;

    public HotelSystem(String hotelName) throws SQLException {
        this.hotelName = hotelName;
        this.dbHandler = new DatabaseHandler();
        this.dbHandler.connect();
    }

    public void startSystem() {
        System.out.println("Hotel System started: " + hotelName);
    }

    public void shutdownSystem() throws SQLException {

        if (dbHandler != null) {
            dbHandler.closeConnection ();
        }
        System.out.println("Hotel System shutdown: " + hotelName);
    }


    public String getHotelName() {
        return hotelName;
    }

    public DatabaseHandler getDbHandler() {
        return dbHandler;
    }
}