package dashboard;

import com.google.gson.reflect.TypeToken;
import data.PermissionStatus;
import data.PermissionType;
import data.PermissionUserData;
import data.SheetUserData;
import engine.EngineManager;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML
    public void initialize() {

        // Set up the cell value factories for available sheets
        sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        permissionTypeColumn.setCellValueFactory(cellData -> {
            SheetUserData sheetData = cellData.getValue();

            // Find the PermissionUserData for the current user
            PermissionUserData permissionData = sheetData.getUserPermissions().stream()
                    .filter(permission -> permission.getUsername().equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            // Check if permission data exists and the status of the permission
            if (permissionData != null) {
                if (Objects.requireNonNull(permissionData.getStatus()) == PermissionStatus.ACKNOWLEDGED) {
                    return new SimpleStringProperty(permissionData.getPermissionType().name());
                }
                return new SimpleStringProperty(PermissionType.NONE.name());
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
                if (response.isSuccessful()) {
                    // Parse the response body to get the list of PermissionUserData objects
                    String responseBody = response.body().string();
                    Set<PermissionUserData> permissions = gson.fromJson(responseBody, new TypeToken<Set<PermissionUserData>>() {}.getType());

                    // Update the UI (permissions table) on the JavaFX thread
                    Platform.runLater(() -> updatePermissionsTable(new ArrayList<>(permissions)));
                } else {
                    // Handle non-successful responses
                    Platform.runLater(() -> showError("Failed to retrieve permissions: Server error"));
                }
            }
        });
    }



    private void updatePermissionsTable(List<PermissionUserData> permissions) {
        permissionsTable.getItems().clear();
        permissionsTable.getItems().addAll(permissions);
    }

    @FXML
    private void handleRequestPermission(ActionEvent event) {
        if (selectedSheet != null) {
            // Allow the user to choose the requested permission (e.g., READER or WRITER)
            ChoiceDialog<PermissionType> permissionDialog = new ChoiceDialog<>(PermissionType.READER, PermissionType.values());
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
                        if (response.isSuccessful()) {
                            Platform.runLater(() -> showSuccess("Permission request sent."));
                        } else {
                            Platform.runLater(() -> showError("Failed to request permission."));
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
                                if (response.isSuccessful()) {
                                    Platform.runLater(() -> showSuccess("Permission updated."));
                                    fetchPermissionsForSelectedSheet(selectedSheet.getSheetName());
                                } else {
                                    Platform.runLater(() -> showError("Failed to update permission."));
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
                            if (response.isSuccessful()) {
                                Platform.runLater(() -> showSuccess("Permission request denied."));
                                fetchPermissionsForSelectedSheet(selectedSheet.getSheetName());
                            } else {
                                Platform.runLater(() -> showError("Failed to deny permission."));
                            }
                        }
                    });
                }
            }
        }
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




