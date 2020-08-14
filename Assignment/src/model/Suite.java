package model;

import exception.DatabaseException;
import exception.MaintenanceException;
import exception.RentRoomException;

import static exception.MaintenanceException.MaintenanceError.InvalidInterval;
import static exception.RentRoomException.RentRoomError.ExceedMaintenance;
import static exception.RentRoomException.RentRoomError.RequiresMaintenance;

public class Suite extends Room {

    private static final double RENTAL_FEE_PER_DAY = 999;
    private static final double MIN_RENT_DAY = 1;
    private static final double LATE_RATE = 1099;

    public Suite(String roomId, int numOfBedrooms, String featureSummary, int roomType,
                 DateTime latestMaintenanceDate) {
        super(roomId, numOfBedrooms, featureSummary, roomType);
        super.setLatestMaintenanceDate(latestMaintenanceDate);
    }

    // Constructor for create suite from Database
    Suite(String roomId, String featureSummary, int roomStatus,
          DateTime latestMaintenanceDate, String image) {

        super(roomId, 6, featureSummary,
                2, roomStatus, latestMaintenanceDate, image);
    }

    @Override
    public String getRoomType() {
        return new String("Suite");
    }

    @Override
    public double getPrice() {
        return RENTAL_FEE_PER_DAY;
    }

    @Override
    public void checkRentDays(DateTime rentDate, int numOfRentDays) throws RentRoomException {

        // rent date should be later than the latest maintenance date
        // Maintenance for a Suite must be done no more than 10 days after its last
        // maintenance date
        int gap = DateTime.diffDays(rentDate, super.getLatestMaintenanceDate());
        if (gap < 0 || gap >= 10) {
            throw new RentRoomException(RequiresMaintenance);
        }

        // rent date should not exceed the date of next maintenance
        DateTime nextMaintenanceDate = new DateTime(super.getLatestMaintenanceDate(), 10);
        int maxRentDays = DateTime.diffDays(nextMaintenanceDate, rentDate);
        if (numOfRentDays > maxRentDays) {
            throw new RentRoomException(ExceedMaintenance);
        }
    }

    @Override
    public double calRentFee(double actualDays, double estDays) {

        double rentFee = 0;

        // if the customer returns the room within the same day, it will be charged
        // for minimum 1 day fee

        if (actualDays < 1) {
            rentFee = RENTAL_FEE_PER_DAY * MIN_RENT_DAY;
        }
        // if the customer returns the room earlier
        else if (actualDays <= estDays) {
            rentFee = RENTAL_FEE_PER_DAY * actualDays;
        } else {
            rentFee = RENTAL_FEE_PER_DAY * estDays;
        }

        return rentFee;
    }

    @Override
    public double calLateFee(double actualDays, double estDays) {
        double lateFee = 0;

        if (actualDays <= estDays) {
            lateFee = 0;
        } else {
            lateFee = LATE_RATE * (actualDays - estDays);
        }

        return lateFee;
    }

    @Override
    public void completeMaintenance(DateTime completionDate) throws MaintenanceException, DatabaseException {

        int gap = DateTime.diffDays(
                completionDate, super.getLatestMaintenanceDate());
        if (gap < 0 || gap > 10) {
            throw new MaintenanceException(InvalidInterval);
        }

        super.completeMaintenance(completionDate);
    }
}
