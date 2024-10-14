package login;

import com.google.gson.Gson;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.Constants;
import utils.http.HttpClientUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML
    private TextField userNameTextField;

    @FXML
    private Label errorMessageLabel;

    private Stage stage;  // The current stage (window)
    private final Gson gson = new Gson();

    // Inject the stage when the controller is created
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Handle the login button click event
    @FXML
    private void loginButtonClicked(ActionEvent event) {
        String userName = userNameTextField.getText();
        if (userName.isEmpty()) {
            errorMessageLabel.setText("User name is empty. You can't login with an empty user name.");
            return;
        }

        HttpUrl loginUrl = HttpUrl.parse(Constants.LOGIN_PAGE).newBuilder().build();
        RequestBody formBody = new FormBody.Builder()
                .add("username", userName)
                .build();

        Request request = new Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> errorMessageLabel.setText("Failed to connect to server: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.code() != 200) {
                    Map<String, String> jsonResponse = gson.fromJson(responseBody, HashMap.class);
                    Platform.runLater(() -> errorMessageLabel.setText("Error: " + jsonResponse.get("error")));
                } else {
                    Map<String, String> jsonResponse = gson.fromJson(responseBody, HashMap.class);
                    Platform.runLater(() -> {
                        // Switch to the dashboard scene upon successful login
                        switchToDashboardScene(jsonResponse.get("username"));
                    });
                }
            }
        });
    }

    // Handle when a key is typed in the username text field
    @FXML
    private void userNameKeyTyped() {
        // Clear the error message when the user starts typing again
        errorMessageLabel.setText("");
    }

    // Handle the quit button click event
    @FXML
    private void quitButtonClicked(ActionEvent event) {
        // Exit the JavaFX application
        Platform.exit();
    }

    // Switch to the dashboard scene after a successful login
    private void switchToDashboardScene(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the username to it
            DashboardController dashboardController = loader.getController();
            dashboardController.initializeDashboard(username);  // Pass username to the next screen
            dashboardController.setStage(stage);  // Pass the stage

            // Switch the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            errorMessageLabel.setText("Failed to load dashboard: " + e.getMessage());
        }
    }

    public static void switchToLoginScreen(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/login/Login.fxml"));
            Parent root = loader.load();

            // Get the controller associated with login.fxml
            LoginController loginController = loader.getController();

            // Pass the stage to the login controller
            loginController.setStage(stage);

            // Set the new scene on the stage and show it
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
