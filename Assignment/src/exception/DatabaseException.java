package exception;

public class DatabaseException extends AppException {

    public DatabaseException() {
        super("Failed to access or update database!");
    }
}
