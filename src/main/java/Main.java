import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/sample.fxml"));
        primaryStage.setTitle("Queuing Simulator");
        primaryStage.setScene(new Scene(root, 352, 270));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
