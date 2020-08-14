package view;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.DateTime;
import util.Utility;

import static util.Utility.showAlert;

class DialogReturnRoom extends Dialog<DateTime> {

    DialogReturnRoom() {
        setTitle("Return Room");

        // init
        GridPane pane = new GridPane();
        Label returnDateLabel = new Label("Return date (dd/mm/yyyy):");
        TextField returnDate = new TextField();
        pane.add(returnDateLabel, 1, 1);
        pane.add(returnDate, 2, 1);
        getDialogPane().setContent(pane);

        ButtonType returnRoom = new ButtonType("Return", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(returnRoom, cancel);

        setResultConverter(param -> {

            if (param == returnRoom) {

                if (!returnDate.getText().matches(
                        "([0-9]{2})/([0-9]{2})/([0-9]{4})")) {
                    showAlert("Error when returning room: Invalid return date.");
                    return null;
                }

                return Utility.stringToDate(returnDate.getText());
            }

            return null;
        });
    }
}
