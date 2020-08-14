package model;

import db.MyDB;
import db.RoomTableContract;
import exception.DatabaseException;
import exception.MaintenanceException;
import exception.RentRoomException;
import exception.ReturnRoomException;
import util.Utility;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static exception.MaintenanceException.MaintenanceError.NotAvailable;
import static exception.RentRoomException.RentRoomError.InvalidRentalDays;
import static exception.RentRoomException.RentRoomError.NotAvailableToRent;
import static exception.ReturnRoomException.ReturnRoomError.NotAvailableToReturn;

public abstract class Room {

    private String roomId;
    private int numOfBedrooms;
    private String featureSummary;
    private int roomType;
    private int roomStatus;
    private int numOfRecord;
    private HiringRecord[] records = new HiringRecord[10];
    private DateTime latestMaintenanceDate;

    private String image = "default.png";

    public abstract String getRoomType();

    public abstract double getPrice();

    public abstract void checkRentDays(DateTime rentdate, int numOfRentDays) throws RentRoomException;

    public abstract double calRentFee(double actualDays, double estDays);

    public abstract double calLateFee(double actualDays, double estDays);

    public Room(String roomId, int numOfBedrooms, String featureSummary, int roomType) {
        this.roomId = roomId;
        this.numOfBedrooms = numOfBedrooms;
        this.featureSummary = featureSummary;
        this.roomType = roomType;
        this.roomStatus = 0;
        this.numOfRecord = 0;
    }

    // Constructor for create record from Database
    protected Room(String roomId, int numOfBedrooms, String featureSummary, int roomType,
                   int roomStatus, DateTime latestMaintenanceDate, String image) {

        this.roomId = roomId;
        this.numOfBedrooms = numOfBedrooms;
        this.featureSummary = featureSummary;
        this.roomType = roomType;
        this.roomStatus = roomStatus;
        this.numOfRecord = 0;
        this.latestMaintenanceDate = latestMaintenanceDate;
        this.image = image;
    }

    public static Room map(ResultSet resultSet) throws SQLException {

        if (resultSet == null) {
            return null;
        }

        String id = resultSet.getString(RoomTableContract.COL_ID);
        int numOfBed = resultSet.getInt(RoomTableContract.COL_NUM_OF_BED);
        String summary = resultSet.getString(RoomTableContract.COL_SUMMARY);
        int type = resultSet.getInt(RoomTableContract.COL_TYPE);
        int status = resultSet.getInt(RoomTableContract.COL_STATUS);
        String latestMaintenanceDate = resultSet.getString(RoomTableContract.COL_LATEST_MAINTENANCE_DATE);
        String image = resultSet.getString(RoomTableContract.COL_IMAGE);

        Room room;
        if (type == 2) {
            room = new Suite(id, summary, status, Utility.stringToDate(latestMaintenanceDate), image);
        } else {
            room = new StandardRoom(id, numOfBed, summary, status, image);
            if (latestMaintenanceDate != null) {
                room.setLatestMaintenanceDate(Utility.stringToDate(latestMaintenanceDate));
            }
        }

        return room;
    }

    public String getImage() {
        return image == null ? "default.png" : image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getNumOfBedrooms() {
        return numOfBedrooms;
    }

    public String getFeatureSummary() {
        return featureSummary;
    }

    public String getRoomStatus() {
        String status = "";

        if (roomStatus == 0) {
            status = "Available";
        } else if (roomStatus == 1) {
            status = "Rented by customer";
        } else if (roomStatus == 2) {
            status = "Under Maintenance";
        }

        return status;
    }

    public void setRoomStatus(int roomStatus) {
        this.roomStatus = roomStatus;
    }

    public DateTime getLatestMaintenanceDate() {
        return latestMaintenanceDate;
    }

    public void setLatestMaintenanceDate(DateTime date) {
        latestMaintenanceDate = date;
    }

    public HiringRecord getLatestRecord() {
        return records[0];
    }

    public List<HiringRecord> getRentalRecords() {

        List<HiringRecord> hiringRecords = new ArrayList<>();
        for (HiringRecord record : records) {
            if (record != null) {
                hiringRecords.add(0, record);
            }
        }

        return hiringRecords;
    }

    public void rent(String customerId, DateTime rentDate, int numOfRentDay)
            throws RentRoomException, DatabaseException {

        if (roomStatus != 0) {
            throw new RentRoomException(NotAvailableToRent);
        }

        // Check for the minimum requirement of rental days
        checkRentDays(rentDate, numOfRentDay);

        // rent day should be later than last actual return date
        if (numOfRecord > 0) {
            int gap = DateTime.diffDays(records[0].getActualReturnDate(), rentDate);
            if (gap > 0) {
                throw new RentRoomException(InvalidRentalDays);
            }
        }

        HiringRecord record = new HiringRecord(roomId, customerId, rentDate, numOfRentDay);
        try {
            // update record and change room status in database
            MyDB.getInstance().rentRoom(roomId, customerId, record);
            // update record and change room status in application
            appendRecord(roomId, customerId, rentDate, numOfRentDay);
            setRoomStatus(1);
        } catch (SQLException e) {
            throw new DatabaseException();
        }
    }

    // add new record to the head of record array
    public void appendRecord(String roomId, String customerId, DateTime rentDate, int numOfRentDay) {
        int index = numOfRecord;

        if (index == 10) {
            index--;
        }

        for (; index > 0; index--) {
            records[index] = records[index - 1];
        }
        records[0] = new HiringRecord(roomId, customerId, rentDate, numOfRentDay);
        numOfRecord++;
    }

    public void appendRecord(HiringRecord hiringRecord) {
        int index = numOfRecord;

        if (index == 10) {
            index--;
        }

        for (; index > 0; index--) {
            records[index] = records[index - 1];
        }
        records[0] = hiringRecord;
        numOfRecord++;
    }

    public void returnRoom(DateTime returnDate)
            throws ReturnRoomException, DatabaseException, RentRoomException {

        // property can only be returned if its status is rented
        if (roomStatus != 1) {
            throw new ReturnRoomException(NotAvailableToReturn);
        }
        int actualDays = DateTime.diffDays(returnDate, records[0].getRentDate());

        // check if actual rent days is valid
        checkRentDays(records[0].getRentDate(), actualDays);

        HiringRecord record = records[0];
        int estDays = record.getEstDays();
        double rentalFee = calRentFee(actualDays, estDays);
        double lateFee = calLateFee(actualDays, estDays);
        record.setReturnInfo(returnDate, rentalFee, lateFee);

        try {
            // update record and change room status in database
            MyDB.getInstance().returnRoom(roomId, record);
            // update record and change room status in application
            records[0] = record;
            setRoomStatus(0);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException();
        }
    }

    public void performMaintenance() throws MaintenanceException, DatabaseException {

        // check the status and modify it to under maintenance
        if (roomStatus != 0) {
            throw new MaintenanceException(NotAvailable);
        }

        try {
            MyDB.getInstance().performMaintenance(roomId);
            setRoomStatus(2);
        } catch (SQLException e) {
            throw new DatabaseException();
        }
    }

    public void completeMaintenance(DateTime completionDate)
            throws MaintenanceException, DatabaseException {

        // check the status
        if (roomStatus != 2) {
            throw new MaintenanceException(NotAvailable);
        }

        try {
            MyDB.getInstance().completeMaintenance(roomId, completionDate);
            setRoomStatus(0);
            latestMaintenanceDate = completionDate;
        } catch (SQLException e) {
            throw new DatabaseException();
        }
    }

    @Override
    public String toString() {
        // pre-defined format
        String info = roomId + " : " + numOfBedrooms + " : " + featureSummary + " : "
                + getRoomStatus();

        // if that room is a suite, attribute latestMaintenanceDate is appended
        // roomType = 1 is Standard room, roomType = 2 is Suite
        if (roomType == 2) {
            info += " : " + latestMaintenanceDate.getFormattedDate();
        }

        info += " : " + image;

        return info;
    }

    public String getDetails() {

        String basicInfo = ""; // Basic information of the room

        basicInfo = "\nRoom ID :                  " + roomId + "\n" + "Number Of Bedrooms :       " + numOfBedrooms + "\n"
                + "Feasture Summary :         " + featureSummary + "\n" + "Status :                   " + getRoomStatus()
                + "\n";

        // if the room is a suite, attribute latestMaintenanceDate is appended
        if (roomType == 2) {
            basicInfo += "Last maintenance date:     " + latestMaintenanceDate.getFormattedDate() + "\n";
        }

        String record = ""; // recent rental records

        if (numOfRecord == 0) {
            // if room is rent for first time, there is no previous rental record
            basicInfo += "RENTAL RECORD :            empty\n";
        } else {

            for (int i = 0; i < numOfRecord; i++) {
                record = records[i].getDetails() + "\n--------------------------------------\n";
                basicInfo += "RENTAL RECORD :\n" + record;
            }

        }

        return basicInfo;
    }
}