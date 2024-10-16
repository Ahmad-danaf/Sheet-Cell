package utils;

import javafx.scene.control.Alert;

public class UIHelper {

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);  // You can set a header if you want, or leave it as null
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Choose the type of alert
        alert.setTitle("Info");
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
