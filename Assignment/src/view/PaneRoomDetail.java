package view;

import controller.CityLodgeRentalSystem;
import db.MyDB;
import exception.DatabaseException;
import exception.MaintenanceException;
import exception.RentRoomException;
import exception.ReturnRoomException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import model.DateTime;
import model.HiringRecord;
import model.Room;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static util.Utility.showAlert;

public class PaneRoomDetail extends GridPane {

    private final CityLodgeRentalSystem cityLodgeRentalSystem;
    private final String roomId;

    private ObservableList<String> recordsData = FXCollections.observableArrayList();

    @FXML
    private ImageView image;
    @FXML
    private Label name;
    @FXML
    private Label price;
    @FXML
    private Label numOfBeds;
    @FXML
    private Label status;
    @FXML
    private Label summary;
    @FXML
    private Label lastMaintenanceDate;
    @FXML
    private ListView<String> records;
    @FXML
    private Button rentRoom;
    @FXML
    private Button returnRoom;
    @FXML
    private Button performMaintenance;
    @FXML
    private Button completeMaintenance;


    public PaneRoomDetail(String roomId, CityLodgeRentalSystem cityLodgeRentalSystem) throws DatabaseException {
        this.roomId = roomId;
        this.cityLodgeRentalSystem = cityLodgeRentalSystem;
        init();
    }

    private void init() throws DatabaseException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/room_detail.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateView();

        // set button actions
        rentRoom.setOnAction(event -> showRentRoomDialog());
        returnRoom.setOnAction(event -> showReturnRoomDialog());
        performMaintenance.setOnAction(event -> performMaintenance());
        completeMaintenance.setOnAction(event -> showCompleteMaintenanceDialog());
    }

    private void updateView() throws DatabaseException {

        try {
            Room room = MyDB.getInstance().getRoomById(roomId);

            image.setImage(new Image("file:image/" + room.getImage(), 0,
                    230, true, true));
            name.setText(room.getRoomType());
            price.setText("$" + room.getPrice() + " per night");
            numOfBeds.setText(String.valueOf(room.getNumOfBedrooms()));
            status.setText(room.getRoomStatus());
            DateTime lastMaintenance = room.getLatestMaintenanceDate();
            lastMaintenanceDate.setText(lastMaintenance == null ? "N/A" : lastMaintenance.toString());
            
            
            String str = room.getFeatureSummary();
            String parsedStr = str.replaceAll("(.{37})", "$1\n");
            summary.setText(parsedStr);
            //summary.setText(room.getFeatureSummary());
            records.setItems(recordsData);
            recordsData.clear();
            for (HiringRecord record : room.getRentalRecords()) {
                recordsData.add(record.getDetails());
            }

        } catch (SQLException e) {
            throw new DatabaseException();
        }

    }

    private void showRentRoomDialog() {

        Optional<DialogRentRoom.Result> result =
                new DialogRentRoom().showAndWait();
        result.ifPresent(rentRoomResult -> {
            try {
                cityLodgeRentalSystem.rentRoom(
                        roomId, rentRoomResult.customerId,
                        rentRoomResult.rentDate, rentRoomResult.rentDays);
                updateView();
            } catch (DatabaseException | RentRoomException e) {
                showAlert(e.getMessage());
            }
        });
    }

    private void showReturnRoomDialog() {

        Optional<DateTime> result =
                new DialogReturnRoom().showAndWait();
        result.ifPresent(returnDate -> {
            try {
                cityLodgeRentalSystem.returnRoom(roomId, returnDate);
                updateView();
            } catch (DatabaseException | ReturnRoomException | RentRoomException e) {
                showAlert(e.getMessage());
            }
        });
    }

    private void performMaintenance() {
        try {
            cityLodgeRentalSystem.roomMaintenance(roomId);
            updateView();
        } catch (MaintenanceException | DatabaseException e) {
            showAlert(e.getMessage());
        }
    }

    private void showCompleteMaintenanceDialog() {

        Optional<DateTime> result =
                new DialogCompleteMaintenance().showAndWait();
        result.ifPresent(completedDate -> {
            try {
                cityLodgeRentalSystem.completeMaintenance(roomId, completedDate);
                updateView();
            } catch (DatabaseException | MaintenanceException e) {
                showAlert(e.getMessage());
            }
        });
    }
}
