package exception;

public class RentRoomException extends AppException {

    public RentRoomException(RentRoomError error) {
        super("Error when renting room: " + error.errorMsg);
    }

    public enum RentRoomError {

        NotAvailableToRent("Selected room is not available to rent."),
        InvalidRentalDays("Invalid rental date and days."),
        RequiresMaintenance("Selected room requires maintenance before renting."),
        ExceedMaintenance("Rental date exceeds next maintenance date."),
        ExceedMaxRentalDays("Standard room can be rented for maximum 10 days."),
        ExceedMinRentalDays("Since the rental day is Saturday or Sunday, the Standard room should be rented from a minimum of 3 days."),
        InvalidCustomerId("Invalid customer id.");

        String errorMsg;

        RentRoomError(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }
}
