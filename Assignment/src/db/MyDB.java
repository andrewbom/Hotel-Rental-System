package db;

import model.DateTime;
import model.HiringRecord;
import model.Room;
import util.Utility;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyDB {

    private static MyDB instance = null;

    public static MyDB getInstance() {
        if (instance == null) {
            instance = new MyDB();
        }
        return instance;
    }

    private final static String DATABASE_URL = "jdbc:hsqldb:file:database/mydatabase";

    private Connection connection;

    private MyDB() {

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (Exception e) {
            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
            e.printStackTrace();
            return;
        }

        try {
            connection = DriverManager.getConnection(DATABASE_URL, "RA", "");

            // try create room and record table
            connection.createStatement().executeUpdate(RoomTableContract.CREATE_ROOM_TABLE);
            connection.createStatement().executeUpdate(RecordTableContract.CREATE_RECORD_TABLE);
            tryGenerateDummyData();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void destroy() throws SQLException {

        if (connection != null) {
            connection.close();
        }
    }

    public List<Room> getRooms() throws SQLException {

        List<Room> rooms = new ArrayList<>();

        ResultSet resultSet = connection
                .createStatement()
                .executeQuery("SELECT * FROM " + RoomTableContract.TABLE_NAME);

        while (resultSet.next()) {
            rooms.add(mapRoomFromResultSet(resultSet));
        }

        return rooms;
    }

    public Room getRoomById(String roomId) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + RoomTableContract.TABLE_NAME +
                        " WHERE " + RoomTableContract.COL_ID + "=?");
        statement.setString(1, roomId);

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return mapRoomFromResultSet(resultSet);
        }

        return null;
    }

    public void addRoom(Room room) throws SQLException {

        int roomType = room.getRoomType().equals("Suite") ? 2 : 1;

        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + RoomTableContract.TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?)");
        statement.setString(1, room.getRoomId());
        statement.setInt(2, room.getNumOfBedrooms());
        statement.setString(3, room.getFeatureSummary());
        statement.setInt(4, roomType);
        statement.setInt(5, 0);
        statement.setString(6, roomType == 2 ?
                room.getLatestMaintenanceDate().toString() : null);
        statement.setString(7, room.getImage());
        statement.executeUpdate();
    }

    public void rentRoom(String roomId, String customerId, HiringRecord record) throws SQLException {

        // insert new record
        PreparedStatement insertRecord = connection.prepareStatement(
                "INSERT INTO " + RecordTableContract.TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        insertRecord.setString(1, record.getRecordId());
        insertRecord.setString(2, roomId);
        insertRecord.setString(3, customerId);
        insertRecord.setString(4, record.getRentDate().toString());
        insertRecord.setString(5, record.getEstimatedReturnDate().toString());
        insertRecord.setString(6, record.getActualReturnDate() == null ?
                null : record.getActualReturnDate().toString());
        insertRecord.setDouble(7, record.getRentalFee());
        insertRecord.setDouble(8, record.getLateFee());
        insertRecord.setBoolean(9, record.getReturned());
        insertRecord.executeUpdate();

        updateRoomStatus(roomId, 1);
    }

    public void returnRoom(String roomId, HiringRecord record) throws SQLException {

        // update record
        PreparedStatement updateRecord = connection.prepareStatement(
                "UPDATE " + RecordTableContract.TABLE_NAME + " SET " +
                        RecordTableContract.COL_ACTUAL_RETURN_DATE + "=?, " +
                        RecordTableContract.COL_RENTAL_FEE + "=?, " +
                        RecordTableContract.COL_LATE_FEE + "=?, " +
                        RecordTableContract.COL_RETURNED + "=?" +
                        " WHERE " + RecordTableContract.COL_ID + "=?");
        updateRecord.setString(1, record.getActualReturnDate().toString());
        updateRecord.setDouble(2, record.getRentalFee());
        updateRecord.setDouble(3, record.getLateFee());
        updateRecord.setBoolean(4, record.getReturned());
        updateRecord.setString(5, record.getRecordId());
        updateRecord.executeUpdate();

        updateRoomStatus(roomId, 0);
    }

    public void performMaintenance(String roomId) throws SQLException {
        updateRoomStatus(roomId, 2);
    }

    public void completeMaintenance(String roomId, DateTime completionDate) throws SQLException {

        PreparedStatement updateRoomStatus = connection.prepareStatement(
                "UPDATE " + RoomTableContract.TABLE_NAME + " SET " +
                        RoomTableContract.COL_STATUS + "=?, " +
                        RoomTableContract.COL_LATEST_MAINTENANCE_DATE + "=?" +
                        " WHERE " + RoomTableContract.COL_ID + "=?");
        updateRoomStatus.setInt(1, 0);
        updateRoomStatus.setString(2, completionDate.toString());
        updateRoomStatus.setString(3, roomId);
        updateRoomStatus.executeUpdate();
    }

    public void saveToFile(File file) throws FileNotFoundException, SQLException {

        PrintWriter printWriter = new PrintWriter(file);
        List<Room> rooms = getRooms();
        for (Room room : rooms) {
            printWriter.println(room.toString());
            List<HiringRecord> records = room.getRentalRecords();
            for (int i = records.size(); i-- > 0; ) {
                printWriter.println(records.get(i).toString());
            }
        }

        printWriter.close();
    }

    public void readFromFile(File file) throws IOException, SQLException {

        List<String> roomsData = new ArrayList<>();
        List<String> recordsData = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null) {
            if (line.contains(" : ")) {
                roomsData.add(line);
            } else {
                recordsData.add(line);
            }
            line = reader.readLine();
        }
        reader.close();

        // we only drop existing data in database if there is any new data
        if (roomsData.size() <= 0) {
            return;
        }

        // delete all rows from rooms and records table
        connection.createStatement()
                .executeUpdate("DELETE FROM " + RoomTableContract.TABLE_NAME);
        connection.createStatement()
                .executeUpdate("DELETE FROM " + RecordTableContract.TABLE_NAME);

        // insert room data to database
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + RoomTableContract.TABLE_NAME + " VALUES ");
        for (String roomData : roomsData) {

            // determine this is a standard room or suite data
            Matcher matcher = Pattern.compile("([0-9]{2})/([0-9]{2})/([0-9]{4})").matcher(roomData);
            boolean isSuite = matcher.find();

            String[] data = roomData.split(" : ");

            String id = data[0];
            int beds = Integer.parseInt(data[1]);
            String summary = data[2];
            int status;
            switch (data[3]) {
                case "Rented by customer":
                    status = 1;
                    break;
                case "Under Maintenance":
                    status = 2;
                    break;
                default:
                    status = 0;
            }

            String lastMaintenanceDate = isSuite ? data[4] : null;
            String image = isSuite ? data[5] : data[4];

            sb.append("('").append(id).append("', ").append(beds)
                    .append(", '").append(summary).append("', ")
                    .append(isSuite ? 2 : 1).append(", ").append(status);

            if (lastMaintenanceDate != null) {
                sb.append(", '").append(lastMaintenanceDate).append("', '").append(image).append("'),");
            } else {
                sb.append(", NULL, '").append(image).append("'),");
            }
        }
        String query = sb.toString();
        query = query.substring(0, query.length() - 1); // remove last , from query
        connection.createStatement().executeUpdate(query);

        //insert record data to database
        sb = new StringBuilder();
        sb.append("INSERT INTO " + RecordTableContract.TABLE_NAME + " VALUES ");

        for (String recordData : recordsData) {

            String[] data = recordData.split(":");

            // extract data
            String id = data[0];
            String[] ids = Utility.extractDataFromRecordId(id);
            String roomId = ids[0];
            String customerId = ids[1];
            String rentalDate = data[1];
            String estimateReturnDate = data[2];
            String actualReturnDate = data[3].equals("none") ? null : data[3];
            double rentalFee = data[4].equals("none") ? 0 : Double.parseDouble(data[4]);
            double lateFee = data[5].equals("none") ? 0 : Double.parseDouble(data[5]);
            boolean returned = !data[4].equals("none");

            sb.append("('").append(id).append("', '").append(roomId)
                    .append("', '").append(customerId).append("', '")
                    .append(rentalDate).append("', '").append(estimateReturnDate);

            if (returned) {

                sb.append("', '").append(actualReturnDate).append("', ")
                        .append(rentalFee).append(", ").append(lateFee)
                        .append(", TRUE),");
            } else {
                sb.append("', NULL, ").append(rentalFee).append(", ")
                        .append(lateFee).append(", FALSE),");
            }
        }
        query = sb.toString();
        query = query.substring(0, query.length() - 1); // remove last , from query
        connection.createStatement().executeUpdate(query);
    }

    private void updateRoomStatus(String roomId, int status) throws SQLException {

        PreparedStatement updateRoomStatus = connection.prepareStatement(
                "UPDATE " + RoomTableContract.TABLE_NAME +
                        " SET " + RoomTableContract.COL_STATUS + "=?" +
                        " WHERE " + RoomTableContract.COL_ID + "=?");
        updateRoomStatus.setInt(1, status);
        updateRoomStatus.setString(2, roomId);
        updateRoomStatus.executeUpdate();
    }

    private void tryGenerateDummyData() throws SQLException {

        ResultSet resultSet = connection
                .createStatement()
                .executeQuery("SELECT COUNT(*) FROM " + RoomTableContract.TABLE_NAME);
        int count = 0;
        while (resultSet.next()) {
            count = resultSet.getInt(1);
        }

        // room table already has data
        if (count > 0) {
            return;
        }

        // insert dummy data into room table
        String today = new DateTime().toString();
        connection.createStatement()
                .executeUpdate("INSERT INTO " + RoomTableContract.TABLE_NAME + " VALUES " +
                        "('R_01', 2, 'This is a standard room with 2 beds.', 1, 1, '" + today + "', 'standard_room_01.jpg')," +
                        "('R_02', 2, 'This is a standard room with 2 beds.', 1, 2, '" + today + "', 'standard_room_02.jpg')," +
                        "('R_03', 2, 'This is a standard room with 2 beds.', 1, 0, '" + today + "', 'standard_room_03.jpg')," +
                        "('R_04', 2, 'This is a standard room with 2 beds.', 1, 0, '" + today + "', 'standard_room_04.jpg')," +
                        "('S_01', 6, 'This is a suite with 6 beds.', 2, 1, '" + today + "', 'suite_01.jpg')," +
                        "('S_02', 6, 'This is a suite with 6 beds.', 2, 0, '" + today + "', 'suite_02.jpg')," +
                        "('S_03', 6, 'This is a suite with 6 beds.', 2, 0, '" + today + "', 'suite_03.jpg')");

        // insert dummy data into record table
        String returnDate1 = new DateTime(3).toString();
        String returnDate2 = new DateTime(5).toString();
        connection.createStatement()
                .executeUpdate("INSERT INTO " + RecordTableContract.TABLE_NAME + " VALUES " +
                        "('R_01_john_26092019', 'R_01', 'john', '" + today + "', '" + returnDate1 + "', NULL, 0, 0, FALSE)," +
                        "('S_01_tom_26092019', 'S_01', 'tom', '" + today + "', '" + returnDate2 + "', NULL, 0, 0, FALSE)");
    }

    private Room mapRoomFromResultSet(ResultSet resultSet) throws SQLException {

        if (resultSet == null) {
            return null;
        }

        Room room = Room.map(resultSet);
        List<HiringRecord> records = getRecordsByRoomId(room.getRoomId());
        for (int i = records.size(); i-- > 0; ) {
            room.appendRecord(records.get(i));
        }

        return room;
    }

    private List<HiringRecord> getRecordsByRoomId(String roomId) throws SQLException {

        List<HiringRecord> records = new ArrayList<>();

        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + RecordTableContract.TABLE_NAME +
                        " WHERE " + RecordTableContract.COL_ROOM_ID + "=?");
        statement.setString(1, roomId);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            records.add(HiringRecord.map(resultSet));
        }

        return records;
    }


}
