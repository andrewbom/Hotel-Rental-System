package view;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.DateTime;
import util.Utility;

import static util.Utility.showAlert;

class DialogCompleteMaintenance extends Dialog<DateTime> {

    DialogCompleteMaintenance() {
        setTitle("Complete Maintenance");

        // init
        GridPane pane = new GridPane();
        Label completedDateLabel = new Label("Completed date (dd/mm/yyyy):");
        TextField completedDate = new TextField();
        pane.add(completedDateLabel, 1, 1);
        pane.add(completedDate, 2, 1);
        getDialogPane().setContent(pane);

        ButtonType complete = new ButtonType("Complete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(complete, cancel);

        setResultConverter(param -> {

            if (param == complete) {

                if (!completedDate.getText().matches(
                        "([0-9]{2})/([0-9]{2})/([0-9]{4})")) {
                    showAlert("Error when completing maintenance: Invalid completed date.");
                    return null;
                }

                return Utility.stringToDate(completedDate.getText());
            }

            return null;
        });
    }
}
