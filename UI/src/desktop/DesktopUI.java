package desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DesktopUI extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main FXML file (body.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("body/body.fxml"));
            Scene scene = new Scene(loader.load());

            // Set the scene to the stage
            primaryStage.setScene(scene);
            primaryStage.setTitle("ShtiCell Spreadsheet");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Main method to launch the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}
