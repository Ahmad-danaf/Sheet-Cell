package dashboard;

import com.google.gson.reflect.TypeToken;
import data.PermissionUserData;
import data.SheetUserData;
import engine.EngineManager;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.UIHelper;
import utils.http.HttpClientUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DashboardController {

    @FXML
    private Label usernameLabel;
    @FXML
    private TableView<SheetUserData> availableSheetsTable;
    @FXML
    private TableView<PermissionUserData> permissionsTable;
    @FXML
    private TableColumn<SheetUserData, String> sheetNameColumn;
    @FXML
    private TableColumn<SheetUserData, String> ownerColumn;
    @FXML
    private TableColumn<SheetUserData, String> sizeColumn;
    @FXML
    private TableColumn<PermissionUserData, String> usernameColumn;
    @FXML
    private TableColumn<PermissionUserData, String> permissionTypeColumn;
    @FXML
    private TableColumn<PermissionUserData, String> statusColumn;

    private final Gson gson = new Gson();
    private Stage stage;
    private EngineManager engineManager;
    private String currentUserId;

    @FXML
    public void initialize() {

        // Set up the cell value factories for available sheets
        sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        // Set up the cell value factories for permissions
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        permissionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("permissionType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    // Initialize the dashboard with the username
    public void initializeDashboard(String username) {
        usernameLabel.setText("Welcome, " + username);
        this.currentUserId = username;
        fetchAllSheetsFromServer();
    }

    @FXML
    private void handleLoadFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML Sheet File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Send the selected file to the server
            uploadFileToServer(selectedFile);
        }
    }

    private void uploadFileToServer(File file) {
        try {
            // Create a multipart request body
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("fileName", file.getName());
            builder.addFormDataPart("file", file.getName(),
                    RequestBody.create(file, MediaType.parse("application/xml")));

            RequestBody requestBody = builder.build();

            // Create the request
            Request request = new Request.Builder()
                    .url("http://localhost:8080/webapp/upload")
                    .post(requestBody)
                    .build();

            // Send the request asynchronously
            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> {
                        // Show error message
                        showError("Failed to upload file: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseBody = response.body().string();
                    Map<String, Object> jsonResponse = gson.fromJson(responseBody, HashMap.class);

                    if (response.isSuccessful()) {
                        // If the upload was successful, update the table
                        Platform.runLater(() -> {
                            showSuccess((String) jsonResponse.get("message"));
                            // Retrieve the "sheets" from the response, handling nulls gracefully
                            Set<SheetUserData> sheets = gson.fromJson(
                                    gson.toJson(jsonResponse.get("sheets")),
                                    new TypeToken<Set<SheetUserData>>(){}.getType()
                            );
                            updateAvailableSheetsTable(sheets);
                        });
                    }  else {
                        // Show error message
                        Platform.runLater(() -> {
                            showError((String) jsonResponse.get("error"));
                        });
                    }
                }
            });

        } catch (Exception e) {
            showError("An error occurred: " + e.getMessage());
        }
    }

    private void fetchAllSheetsFromServer() {
        Request request = new Request.Builder()
                .url("http://localhost:8080/webapp/getAllSheets")
                .get()
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to retrieve sheets: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                Set<SheetUserData> sheets = gson.fromJson(responseBody, new TypeToken<Set<SheetUserData>>(){}.getType());

                Platform.runLater(() -> updateAvailableSheetsTable(sheets));
            }
        });
    }


    private void updateAvailableSheetsTable(Set<SheetUserData> sheets) {
        // Clear the existing items in the table
        availableSheetsTable.getItems().clear();

        // Add each sheet directly to the table
        availableSheetsTable.getItems().addAll(sheets);
    }

    private void showError(String message) {
        // Show an error message to the user, e.g., using an alert
        System.err.println("Error: " + message);
        UIHelper.showError(message);
    }

    private void showSuccess(String message) {
        // Show a success message to the user, e.g., using an alert
        System.out.println("Success: " + message);
    }
}




