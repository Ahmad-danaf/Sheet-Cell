package dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import login.LoginController;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    private Stage stage;

    // This method will be called after login to pass the username
    public void initializeDashboard(String username) {
        welcomeLabel.setText("Welcome, " + username + "!");
    }

    @FXML
    private void logoutButtonClicked(ActionEvent event) {
        // Handle logout and return to the login screen
        LoginController.switchToLoginScreen(stage);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
