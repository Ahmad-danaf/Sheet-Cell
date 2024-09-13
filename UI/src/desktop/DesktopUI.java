package desktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class DesktopUI extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello JavaFX!");

        Label helloLabel = new Label("Hello, JavaFX!");
        StackPane root = new StackPane();
        root.getChildren().add(helloLabel);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
