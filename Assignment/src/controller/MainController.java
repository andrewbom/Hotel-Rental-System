package controller;

import db.MyDB;
import exception.DatabaseException;
import exception.MaintenanceException;
import exception.RentRoomException;
import exception.ReturnRoomException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import model.DateTime;
import model.Room;
import view.DialogNewRoom;
import view.ListViewRooms;
import view.PaneRoomDetail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import static util.Utility.showAlert;

public class MainController implements Initializable, CityLodgeRentalSystem {

    private ObservableList<Room> rooms =
            FXCollections.observableArrayList();

    @FXML
    private BorderPane pane;
    @FXML
    private MenuBar menuBar;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menuBar.prefWidthProperty().bind(pane.widthProperty());
        showRoomsPage();
    }

    // ---------------- UI event handler methods ----------------
    @FXML
    public void showRoomsPage() {

        // init view
        ListViewRooms listViewRooms = new ListViewRooms(this::showRoomDetail);
        pane.setCenter(listViewRooms);
        listViewRooms.setItems(rooms);

        // get data
        try {
            rooms.clear();
            rooms.addAll(MyDB.getInstance().getRooms());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to get data of rooms!");
        }
    }

    @FXML
    public void showAddRoomPage() {
        Optional<Room> result = new DialogNewRoom().showAndWait();
        result.ifPresent(room -> {
            try {
                addRoom(room);
            } catch (DatabaseException e) {
                showAlert(e.getMessage());
            }
        });
    }

    @FXML
    public void showImportDialog() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedFile = fileChooser.showOpenDialog(pane.getScene().getWindow());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Importing Data");
        alert.setContentText("All data will be overrode. Are you ok with this?");

        Optional<ButtonType> result = alert.showAndWait();
        result.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    MyDB.getInstance().readFromFile(selectedFile);
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
                showRoomsPage();
            }
        });
    }

    @FXML
    public void showExportDialog() {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedDirectory = directoryChooser.showDialog(pane.getScene().getWindow());

        // save data to file
        try {
            File file = new File(selectedDirectory, "export_data.txt");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            MyDB.getInstance().saveToFile(file);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void exit() {
        Platform.exit();
    }
    // ---------------- UI event handler methods ----------------

    // ---------------- utility methods ----------------
    private void showRoomDetail(String roomId) {
        try {
            PaneRoomDetail roomDetail = new PaneRoomDetail(roomId, this);
            pane.setCenter(roomDetail);
        } catch (DatabaseException e) {
            showAlert(e.getMessage());
        }
    }

    private Room getRoomById(String roomId) {

        for (Room room : rooms) {
            if (room.getRoomId().equals(roomId)) {
                return room;
            }
        }

        return null;
    }
    // ---------------- utility methods ----------------

    // ---------------- rental system methods ----------------
    @Override
    public void addRoom(Room room) throws DatabaseException {

        try {
            // add room to database
            MyDB.getInstance().addRoom(room);
            // add room to application
            rooms.add(0, room);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException();
        }
    }

    @Override
    public void rentRoom(String roomId, String customerId, DateTime rentDate, int rentDays)
            throws RentRoomException, DatabaseException {

        Room room = getRoomById(roomId);
        if (room != null) {
            room.rent(customerId, rentDate, rentDays);
        }
    }

    @Override
    public void returnRoom(String roomId, DateTime returnedDate)
            throws DatabaseException, ReturnRoomException, RentRoomException {

        Room room = getRoomById(roomId);
        if (room != null) {
            room.returnRoom(returnedDate);
        }
    }

    @Override
    public void roomMaintenance(String roomId)
            throws MaintenanceException, DatabaseException {

        Room room = getRoomById(roomId);
        if (room != null) {
            room.performMaintenance();
        }
    }

    @Override
    public void completeMaintenance(String roomId, DateTime completionDate)
            throws MaintenanceException, DatabaseException {

        Room room = getRoomById(roomId);
        if (room != null) {
            room.completeMaintenance(completionDate);
        }
    }
    // ---------------- rental system methods ----------------
}
