package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.Room;

import java.io.IOException;

public class ListViewRooms extends ListView<Room> {

    public ListViewRooms(ListViewListener listener) {
        setCellFactory(param -> new ListCellRoom(listener));
    }

    static class ListCellRoom extends ListCell<Room> {

        @FXML
        private HBox pane;

        @FXML
        private ImageView roomImage;

        @FXML
        private Label roomName;

        @FXML
        private Label roomBeds;

        @FXML
        private Label roomAvailability;

        @FXML
        private Label roomSummary;

        @FXML
        private Button roomDetailButton;

        private FXMLLoader fxmlLoader;
        private ListViewListener listener;


        ListCellRoom(ListViewListener listener) {
            super();
            this.listener = listener;
        }

        @Override
        protected void updateItem(Room item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {

                if (fxmlLoader == null) {
                    fxmlLoader = new FXMLLoader(
                            getClass().getResource("/fxml/listcell_room.fxml"));
                    fxmlLoader.setController(this);
                    try {
                        fxmlLoader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                roomName.setText(item.getRoomType());
                roomBeds.setText("Bed(s): " + item.getNumOfBedrooms());
                roomAvailability.setText(item.getRoomStatus());
                roomSummary.setText(item.getFeatureSummary());
                roomImage.setImage(new Image("file:image/" + item.getImage(), 0,
                        100, true, true));
                roomDetailButton.setOnAction(event -> listener.onRoomClicked(item.getRoomId()));

                setText(null);
                setGraphic(pane);
            }
        }
    }

    public interface ListViewListener {
        void onRoomClicked(String roomId);
    }
}
