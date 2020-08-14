package util;

import javafx.scene.control.Alert;
import model.DateTime;

public class Utility {

    //read the String input and change it to dd/mm/yyyy format
    public static DateTime stringToDate(String date) {

        int day = Integer.parseInt(date.substring(0, 2)); // read the day from input dd/mm/yyyy
        int month = Integer.parseInt(date.substring(3, 5)); // read the month from input dd/mm/yyyy
        int year = Integer.parseInt(date.substring(6, 10)); // read the year from input dd/mm/yyyy

        return new DateTime(day, month, year);
    }

    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static String[] extractDataFromRecordId(String recordId) {

        String[] data = recordId.split("_");
        String roomId = data[0] + "_" + data[1];
        String customerId = data[2];

        return new String[]{roomId, customerId};
    }
}
