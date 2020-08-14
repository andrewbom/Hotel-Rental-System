package controller;

import exception.DatabaseException;
import exception.MaintenanceException;
import exception.RentRoomException;
import exception.ReturnRoomException;
import model.DateTime;
import model.Room;

import java.sql.SQLException;

public interface CityLodgeRentalSystem {

    void addRoom(Room room) throws SQLException, DatabaseException;

    void rentRoom(String roomId, String customerId, DateTime rentDate, int rentDays) throws RentRoomException, DatabaseException;

    void returnRoom(String roomId, DateTime returnedDate) throws DatabaseException, ReturnRoomException, RentRoomException;

    void roomMaintenance(String roomId) throws MaintenanceException, DatabaseException;

    void completeMaintenance(String roomId, DateTime completionDate) throws MaintenanceException, DatabaseException;
}
