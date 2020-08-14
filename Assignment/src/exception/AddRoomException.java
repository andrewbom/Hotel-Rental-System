package exception;

public class AddRoomException extends AppException {

    public AddRoomException(AddRoomError error) {
        super("Error when adding new room: " + error.errorMsg);
    }

    public enum AddRoomError {

        DuplicatedRoomId("A room with given Id already exists in system!"),
        InvalidRoomIdFormat("Invalid Room Id, Room ID must start with R_ for Standard room or S_ for Suite."),
        SummaryTooLong("Feature summary could not be more than 20 words."),
        InvalidImageFileName("Invalid image file name."),
        ImageFileNotExist("Cannot find room image in the image folder."),
        InvalidDateFormat("Please enter valid date by following the format (dd/mm/yyyy).");

        String errorMsg;

        AddRoomError(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }
}
