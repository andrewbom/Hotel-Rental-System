package view;

import db.MyDB;
import exception.AddRoomException;
import exception.AppException;
import exception.DatabaseException;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import model.DateTime;
import model.Room;
import model.StandardRoom;
import model.Suite;
import util.Utility;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static exception.AddRoomException.AddRoomError.*;
import static util.Utility.showAlert;

public class DialogNewRoom extends Dialog<Room> {

    private static final String TYPE_STANDARD = "Standard Room";
    private static final String TYPE_SUITE = "Suite";

    private ChoiceBox<String> type;
    private TextField id;
    private TextField summary;
    private TextField image;
    private ChoiceBox<Integer> beds;
    private TextField maintenance;


    public DialogNewRoom() {
        setTitle("Add a New Room");
        init();
    }

    private void init() {

        GridPane pane = new GridPane();

        // labels
        Label typeLabel = new Label("Room type:");
        Label idLabel = new Label("Room ID:");
        Label summaryLabel = new Label("Room description:");
        Label imageLabel = new Label("Image file name (please put image in the image folder):");
        Label bedsLabel = new Label("Number of beds:");
        Label maintenanceLabel = new Label("Last maintenance date (dd/mm/yyyy):");

        // type
        type = new ChoiceBox<>(FXCollections.observableArrayList(
                TYPE_STANDARD, TYPE_SUITE));
        type.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(oldValue)) {
                        updateView(newValue);
                    }
                });

        // other fields
        id = new TextField();
        summary = new TextField();
        image = new TextField();
        beds = new ChoiceBox<>(
                FXCollections.observableArrayList(1, 2, 4));
        maintenance = new TextField();

        // add view to dialog
        pane.add(typeLabel, 1, 1);
        pane.add(type, 2, 1);
        pane.add(idLabel, 1, 2);
        pane.add(id, 2, 2);
        pane.add(summaryLabel, 1, 3);
        pane.add(summary, 2, 3);
        pane.add(imageLabel, 1, 4);
        pane.add(image, 2, 4);
        pane.add(bedsLabel, 1, 5);
        pane.add(beds, 2, 5);
        pane.add(maintenanceLabel, 1, 6);
        pane.add(maintenance, 2, 6);
        getDialogPane().setContent(pane);

        // add dialog buttons
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(save, cancel);

        type.getSelectionModel().selectFirst();
        beds.getSelectionModel().selectFirst();

        setResultConverter(param -> {
            if (param == save) {

                try {
                    checkInputs();
                } catch (AppException e) {
                    showAlert(e.getMessage());
                    return null;
                }

                Room room;
                String roomId = id.getText();
                String roomSummary = summary.getText();

                if (type.getValue().equals(TYPE_SUITE)) {
                    DateTime maintenanceDate = Utility.stringToDate(maintenance.getText());
                    room = new Suite(roomId, 6, roomSummary, 2, maintenanceDate);
                } else {
                    room = new StandardRoom(roomId, beds.getValue(), roomSummary, 1);
                }
                room.setImage(image.getText());

                return room;
            }

            return null;
        });
    }

    private void updateView(String roomType) {

        if (roomType.equals(TYPE_SUITE)) {
            beds.setDisable(true);
            maintenance.setDisable(false);
        } else {
            beds.setDisable(false);
            maintenance.setDisable(true);
        }
    }

    private void checkInputs() throws
            AddRoomException, DatabaseException {

        int roomType = type.getValue().equals(TYPE_SUITE) ? 2 : 1;

        // check id format
        String roomId = id.getText();
        if ((roomType == 1 && !roomId.startsWith("R_"))
                || (roomType == 2 && !roomId.startsWith("S_"))) {
            throw new AddRoomException(InvalidRoomIdFormat);
        }

        // check id existence
        try {
            if (MyDB.getInstance().getRoomById(roomId) != null) {
                throw new AddRoomException(DuplicatedRoomId);
            }
        } catch (SQLException e) {
            throw new DatabaseException();
        }

        // check summary length
        if (summary.getText().split(" ").length > 20) {
            throw new AddRoomException(SummaryTooLong);
        }

        // check image file name and existence
        // reference from:
        // https://www.mkyong.com/regular-expressions/how-to-validate-image-file-extension-with-regular-expression/
        String imageFileName = image.getText();
        Matcher matcher = Pattern.compile("([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)")
                .matcher(imageFileName);
        if (!matcher.matches()) {
            throw new AddRoomException(InvalidImageFileName);
        }

        Image image = new Image("file:image/" + imageFileName, 0,
                100, true, true);
        if (image.isError()) {
            throw new AddRoomException(ImageFileNotExist);
        }

        // check maintenance date format
        if (roomType == 2 && !maintenance.getText().matches(
                "([0-9]{2})/([0-9]{2})/([0-9]{4})")) {
            throw new AddRoomException(InvalidDateFormat);
        }
    }
}
