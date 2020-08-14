package exception;

public class MaintenanceException extends AppException {

    public MaintenanceException(MaintenanceError error) {
        super("Error: " + error.errorMsg);
    }

    public enum MaintenanceError {

        NotAvailable("Selected room is not available for maintenance or completing maintenance."),
        InvalidInterval("All suites must have a maintenance interval of 10 days.");

        String errorMsg;

        MaintenanceError(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }
}
