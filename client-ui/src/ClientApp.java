import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import login.LoginController;  // Ensure this import is added

public class ClientApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {

            // Load the FXML file for the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login/login.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the controller associated with the login.fxml
            LoginController loginController = loader.getController();

            // Pass the primary stage to the login controller
            loginController.setStage(primaryStage);

            // Set the scene to the stage and display the window
            primaryStage.setScene(scene);
            primaryStage.setTitle("ShtiCell Spreadsheet");
            primaryStage.show();
        } catch (Exception e) {
            System.out.println("Failed to start the application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Main method to launch the JavaFX application
    public static void main(String[] args) {
        System.out.println("Launching the application...");
        launch(args);
        System.out.println("Application has been launched.");
    }
}
