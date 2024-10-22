package dashboard;
import com.google.gson.JsonParser;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import data.PermissionStatus;
import data.PermissionType;
import data.PermissionUserData;
import data.SheetUserData;
import engine.EngineManager;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import sheetDisplay.SheetDisplayController;
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
    private TableColumn<SheetUserData, String> permissionTypeColumn;
    @FXML
    private TableColumn<PermissionUserData, String> statusColumn;
    @FXML
    private TableColumn<PermissionUserData, String> userPermissionTypeColumn;

    private final Gson gson = new Gson();
    private Stage stage;
    private EngineManager engineManager;
    private String currentUserId;
    private SheetUserData selectedSheet;
    private ScheduledService<Void> pollingService;


    @FXML
    public void initialize() {

        // Set up the cell value factories for available sheets
        sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        permissionTypeColumn.setCellValueFactory(cellData -> {
            SheetUserData sheetData = cellData.getValue();

            PermissionUserData permissionData = sheetData.getPermissionForUser(currentUserId);
            if (permissionData != null) {
                if (permissionData.getStatus() == PermissionStatus.ACKNOWLEDGED) {
                    return new SimpleStringProperty(permissionData.getPermissionType().name());
                } else{
                    // Show the previous acknowledged permission if a request is pending or deny
                    return new SimpleStringProperty(permissionData.getPrevAcknowledgedPermission().name());
                }

            }
            // Default to NONE if no permission is found
            return new SimpleStringProperty(PermissionType.NONE.name());
        });



        // Set up the cell value factories for permissions
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userPermissionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("permissionType"));
        statusColumn.setCellValueFactory(cellData -> {
            PermissionUserData permission = cellData.getValue();
            switch (permission.getStatus()) {
                case ACKNOWLEDGED:
                    return new SimpleStringProperty("Acknowledged");
                case DENIED:
                    return new SimpleStringProperty("Denied");
                default:
                    return new SimpleStringProperty("Pending");
            }
        });


        availableSheetsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedSheet = newValue;
                        fetchPermissionsForSelectedSheet(newValue.getSheetName());
                    }
                }
        );

        startPollingService();
    }

    private void startPollingService() {
        pollingService = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        // Fetch the updated data from the server
                        Platform.runLater(() -> {
                            fetchAllSheetsFromServer();
                            if (selectedSheet != null) {
                                fetchPermissionsForSelectedSheet(selectedSheet.getSheetName());
                            }
                        });
                        return null;
                    }
                };
            }
        };

        // Set the polling interval to 1 second (adjust as needed)
        pollingService.setPeriod(Duration.seconds(2));

        // Start the polling service
        pollingService.start();
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
                    try (ResponseBody responseBody = response.body()) {  // Automatically close response body
                        if (responseBody == null || !response.isSuccessful()) {
                            Platform.runLater(() -> showError("File upload failed."));
                            return;
                        }

                        String responseBodyString = responseBody.string();
                        Map<String, Object> jsonResponse = gson.fromJson(responseBodyString, HashMap.class);

                        Platform.runLater(() -> {
                            showSuccess((String) jsonResponse.get("message"));
                            // Retrieve the "sheets" from the response
                            Set<SheetUserData> sheets = gson.fromJson(
                                    gson.toJson(jsonResponse.get("sheets")),
                                    new TypeToken<Set<SheetUserData>>(){}.getType()
                            );
                            updateAvailableSheetsTable(sheets);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showError("An error occurred while processing response: " + e.getMessage()));
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
                try (ResponseBody responseBody = response.body()) {  // Automatically close response body
                    if (responseBody == null || !response.isSuccessful()) {
                        Platform.runLater(() -> showError("Failed to retrieve sheets from server."));
                        return;
                    }

                    String responseBodyString = responseBody.string();
                    Set<SheetUserData> sheets = gson.fromJson(responseBodyString, new TypeToken<Set<SheetUserData>>(){}.getType());

                    Platform.runLater(() -> updateAvailableSheetsTable(sheets));
                } catch (Exception e) {
                    Platform.runLater(() -> showError("An error occurred while processing response: " + e.getMessage()));
                }
            }
        });
    }



    private void updateAvailableSheetsTable(Set<SheetUserData> sheets) {
        // Preserve the selected sheet
        SheetUserData selectedSheet = availableSheetsTable.getSelectionModel().getSelectedItem();

        // Clear the existing items in the table
        availableSheetsTable.getItems().clear();

        // Add the new items
        availableSheetsTable.getItems().addAll(sheets);

        // Restore the selected sheet if it is still in the updated list
        if (selectedSheet != null) {
            for (SheetUserData sheet : availableSheetsTable.getItems()) {
                if (sheet.getSheetName().equals(selectedSheet.getSheetName())) {
                    availableSheetsTable.getSelectionModel().select(sheet);
                    break;
                }
            }
        }
    }


    private void fetchPermissionsForSelectedSheet(String sheetName) {
        // Build the request to get permissions for the selected sheet
        Request request = new Request.Builder()
                .url("http://localhost:8080/webapp/getPermissionsForSheet?sheetName=" + sheetName)
                .get()
                .build();

        // Send the request asynchronously
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Handle failure by showing an error message
                Platform.runLater(() -> showError("Failed to retrieve permissions: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {  // Ensure response body is closed
                    if (responseBody == null || !response.isSuccessful()) {
                        Platform.runLater(() -> showError("Failed to retrieve permissions: Server error"));
                        return;
                    }

                    // Parse the response body to get the list of PermissionUserData objects
                    String responseBodyString = responseBody.string();
                    Set<PermissionUserData> permissions = gson.fromJson(responseBodyString, new TypeToken<Set<PermissionUserData>>() {}.getType());

                    // Update the UI (permissions table) on the JavaFX thread
                    Platform.runLater(() -> updatePermissionsTable(new ArrayList<>(permissions)));
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Error processing permissions: " + e.getMessage()));
                }
            }
        });
    }




    private void updatePermissionsTable(List<PermissionUserData> permissions) {
        // Preserve the selected permission
        PermissionUserData selectedPermission = permissionsTable.getSelectionModel().getSelectedItem();

        // Clear the existing items in the table
        permissionsTable.getItems().clear();

        // Add the new items
        permissionsTable.getItems().addAll(permissions);

        // Restore the selected permission if it is still in the updated list
        if (selectedPermission != null) {
            for (PermissionUserData permission : permissionsTable.getItems()) {
                if (permission.getUsername().equals(selectedPermission.getUsername())) {
                    permissionsTable.getSelectionModel().select(permission);
                    break;
                }
            }
        }
    }


    @FXML
    private void handleRequestPermission(ActionEvent event) {
        if (selectedSheet != null) {
            if (selectedSheet.getOwner().equals(currentUserId)) {
                showError("You are the owner of this sheet.");
                return;
            }
            // Allow the user to choose the requested permission (e.g., READER or WRITER)
            ChoiceDialog<PermissionType> permissionDialog = new ChoiceDialog<>(PermissionType.READER,
                    EnumSet.of(PermissionType.READER, PermissionType.WRITER));
            permissionDialog.setTitle("Request Permission");
            permissionDialog.setHeaderText("Choose the permission you want to request:");
            Optional<PermissionType> result = permissionDialog.showAndWait();

            result.ifPresent(requestedPermission -> {
                // Send the permission request to the server
                Request request = new Request.Builder()
                        .url("http://localhost:8080/webapp/requestPermission")
                        .post(new FormBody.Builder()
                                .add("sheetName", selectedSheet.getSheetName())
                                .add("requester", currentUserId)
                                .add("requestedPermission", requestedPermission.name())
                                .build())
                        .build();

                HttpClientUtil.runAsync(request, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Platform.runLater(() -> showError("Failed to request permission: " + e.getMessage()));
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try (ResponseBody responseBody = response.body()) {  // Ensure response body is closed
                            if (responseBody == null || !response.isSuccessful()) {
                                Platform.runLater(() -> showError("Failed to request permission."));
                                return;
                            }

                            Platform.runLater(() -> showSuccess("Permission request sent."));
                        } catch (Exception e) {
                            Platform.runLater(() -> showError("Error processing permission request: " + e.getMessage()));
                        }
                    }
                });
            });
        }
    }



    @FXML
    private void handleAcknowledgeDenyPermission(ActionEvent event) {
        if (selectedSheet != null && currentUserId.equals(selectedSheet.getOwner())) {
            PermissionUserData selectedPermission = permissionsTable.getSelectionModel().getSelectedItem();
            if (selectedPermission != null) {
                // for only pending permissions
                if (selectedPermission.getStatus() != PermissionStatus.PENDING) {
                    showError("Permission is not pending.");
                    return;
                }
                // Allow the owner to choose to acknowledge (approve) or deny the request
                Alert decisionAlert = new Alert(Alert.AlertType.CONFIRMATION);
                decisionAlert.setTitle("Acknowledge or Deny Permission");
                decisionAlert.setHeaderText("Acknowledge or deny the permission request for user: " + selectedPermission.getUsername());
                decisionAlert.setContentText("Choose your action:");

                ButtonType acknowledgeButton = new ButtonType("Acknowledge");
                ButtonType denyButton = new ButtonType("Deny", ButtonBar.ButtonData.CANCEL_CLOSE);

                decisionAlert.getButtonTypes().setAll(acknowledgeButton, denyButton);

                Optional<ButtonType> result = decisionAlert.showAndWait();
                if (result.isPresent() && result.get() == acknowledgeButton) {
                    // The owner acknowledged the request. Allow the owner to choose the permission type to grant.
                    ChoiceDialog<PermissionType> permissionDialog = new ChoiceDialog<>(PermissionType.READER, PermissionType.WRITER);
                    permissionDialog.setTitle("Grant Permission");
                    permissionDialog.setHeaderText("Choose the permission type to grant:");
                    Optional<PermissionType> permissionResult = permissionDialog.showAndWait();

                    permissionResult.ifPresent(grantedPermission -> {
                        // Send the acknowledge request to the server
                        Request request = new Request.Builder()
                                .url("http://localhost:8080/webapp/acknowledgePermission")
                                .post(new FormBody.Builder()
                                        .add("sheetName", selectedSheet.getSheetName())
                                        .add("targetUser", selectedPermission.getUsername())
                                        .add("permission", grantedPermission.name())
                                        .add("status", "ACKNOWLEDGED")
                                        .build())
                                .build();

                        HttpClientUtil.runAsync(request, new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                Platform.runLater(() -> showError("Failed to update permission: " + e.getMessage()));
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                try (ResponseBody responseBody = response.body()) {  // Ensure response body is closed
                                    if (responseBody == null || !response.isSuccessful()) {
                                        Platform.runLater(() -> showError("Failed to update permission."));
                                        return;
                                    }

                                    Platform.runLater(() -> showSuccess("Permission updated."));
                                    fetchPermissionsForSelectedSheet(selectedSheet.getSheetName());
                                } catch (Exception e) {
                                    Platform.runLater(() -> showError("Error updating permission: " + e.getMessage()));
                                }
                            }
                        });
                    });
                } else if (result.isPresent() && result.get() == denyButton) {
                    // The owner denied the request, send denial to the server
                    Request request = new Request.Builder()
                            .url("http://localhost:8080/webapp/acknowledgePermission")
                            .post(new FormBody.Builder()
                                    .add("sheetName", selectedSheet.getSheetName())
                                    .add("targetUser", selectedPermission.getUsername())
                                    .add("permission", PermissionType.NONE.name()) // Set permission to NONE for denial
                                    .add("status", "DENIED") // Explicitly mark as denied
                                    .build())
                            .build();

                    HttpClientUtil.runAsync(request, new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Platform.runLater(() -> showError("Failed to deny permission: " + e.getMessage()));
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            try (ResponseBody responseBody = response.body()) {  // Ensure response body is closed
                                if (responseBody == null || !response.isSuccessful()) {
                                    Platform.runLater(() -> showError("Failed to deny permission."));
                                    return;
                                }

                                Platform.runLater(() -> showSuccess("Permission request denied."));
                                fetchPermissionsForSelectedSheet(selectedSheet.getSheetName());
                            } catch (Exception e) {
                                Platform.runLater(() -> showError("Error denying permission: " + e.getMessage()));
                            }
                        }
                    });
                }
            }
        }
    }

    @FXML
    private void handleViewSheet(ActionEvent event) {
        // Ensure that a sheet is selected from the available sheets table
        if (selectedSheet == null) {
            showError("No sheet selected. Please select a sheet to view.");
            return;
        }

        try {
            // Load the FXML for the sheet display view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sheetDisplay/sheetDisplay.fxml"));
            Parent sheetDisplayParent = loader.load();

            // Get the controller for the sheet display and initialize it with the selected sheet data
            SheetDisplayController sheetDisplayController = loader.getController();
            fetchSheetDataFromServer(selectedSheet.getSheetName(), sheetDisplayController);
            sheetDisplayController.initializeSheetView(selectedSheet,currentUserId); // Passing the selected sheet to the next controller

            // Set up the new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(sheetDisplayParent));
            stage.show();

        } catch (IOException e) {
            showError("Failed to load the sheet display view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchSheetDataFromServer(String sheetName, SheetDisplayController sheetDisplayController) {
        String url = "http://localhost:8080/webapp/getSheetView?sheetName=" + sheetName+"&username=" + currentUserId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showError("Failed to retrieve sheet data: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Platform.runLater(() -> showError("Failed to retrieve sheet data from server."));
                    String errorBody = response.body() != null ? response.body().string() : "UE";
                    // Parse the JSON error response
                    Map<String, String> errorResponse = gson.fromJson(errorBody, Map.class);
                    String errorMessage = errorResponse.getOrDefault("error", "UE");
                    System.out.println(("Error: " + errorMessage));

                    return;
                }

                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null) {
                        // Parse the JSON response into a map
                        String jsonResponse = responseBody.string();
                        Map<String, Object> sheetData = gson.fromJson(jsonResponse, Map.class);


                        // Update the UI (TableView) with the sheet data
                        Platform.runLater(() -> sheetDisplayController.populateTableView(sheetData));
                    }
                }
            }
        });
    }


    private void showError(String message) {
        // Show an error message to the user, e.g., using an alert
        System.err.println("Error: " + message);
        UIHelper.showError(message);
    }

    private void showSuccess(String message) {
        // Show a success message to the user, e.g., using an alert
        System.out.println("Success: " + message);
        UIHelper.showAlert("Success", message);
    }
}




