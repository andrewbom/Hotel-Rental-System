import db.MyDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MyApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        MyDB.getInstance();

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        stage.setTitle("City Lodge Rental System");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        MyDB.getInstance().destroy();
    }
}
