package view;

import exception.AppException;
import exception.RentRoomException;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.DateTime;
import util.Utility;

import static exception.RentRoomException.RentRoomError.InvalidCustomerId;
import static exception.RentRoomException.RentRoomError.InvalidRentalDays;
import static util.Utility.showAlert;

class DialogRentRoom extends Dialog<DialogRentRoom.Result> {

    private TextField customerId;
    private TextField rentalDate;
    private Spinner<Integer> rentDays;

    DialogRentRoom() {
        setTitle("Rent Room");
        init();
    }

    private void init() {

        GridPane pane = new GridPane();

        // labels
        Label customerIdLabel = new Label("Customer ID:");
        Label rentalDateLabel = new Label("Rental date (dd/mm/yyyy):");
        Label rentDaysLabel = new Label("Rent days:");

        // inputs
        customerId = new TextField();
        rentalDate = new TextField();
        rentDays = new Spinner<>(1, 100, 1);

        // add views to dialog
        pane.add(customerIdLabel, 1, 1);
        pane.add(customerId, 2, 1);
        pane.add(rentalDateLabel, 1, 2);
        pane.add(rentalDate, 2, 2);
        pane.add(rentDaysLabel, 1, 3);
        pane.add(rentDays, 2, 3);
        getDialogPane().setContent(pane);

        // add dialog buttons
        ButtonType rent = new ButtonType("Rent", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(rent, cancel);

        setResultConverter(param -> {

            if (param == rent) {

                try {
                    checkInputs();
                } catch (AppException e) {
                    showAlert(e.getMessage());
                    return null;
                }

                return new Result(
                        customerId.getText(),
                        Utility.stringToDate(rentalDate.getText()),
                        rentDays.getValue());
            }

            return null;
        });
    }

    private void checkInputs() throws RentRoomException {

        if (customerId.getText() == null || customerId.getText().length() < 1) {
            throw new RentRoomException(InvalidCustomerId);
        }

        if (!rentalDate.getText().matches(
                "([0-9]{2})/([0-9]{2})/([0-9]{4})")) {
            throw new RentRoomException(InvalidRentalDays);
        }

    }

    public static class Result {

        public final String customerId;
        public final DateTime rentDate;
        public final int rentDays;

        private Result(String customerId, DateTime rentDate, int rentDays) {
            this.customerId = customerId;
            this.rentDate = rentDate;
            this.rentDays = rentDays;
        }
    }
}
