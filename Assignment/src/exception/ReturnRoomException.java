package exception;

public class ReturnRoomException extends AppException {

    public ReturnRoomException(ReturnRoomError error) {
        super("Error when returning room: " + error.errorMsg);
    }

    public enum ReturnRoomError {

        NotAvailableToReturn("Selected room is not available to return."),
        InvalidReturnDate("Invalid return date.");

        String errorMsg;

        ReturnRoomError(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }
}
