package dashboard;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import login.LoginController;

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

    private Stage stage;

    @FXML
    public void initialize() {
        populateFakeData();

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
    }

//    @FXML
//    private void logoutButtonClicked(ActionEvent event) {
//        // Handle logout and return to the login screen
//        LoginController.switchToLoginScreen(stage);
//    }
public void populateFakeData() {
    ObservableList<SheetUserData> sheets = FXCollections.observableArrayList(
            new SheetUserData("Budget 2024", "Alice", "2 MB"),
            new SheetUserData("Project Plan", "Bob", "1.5 MB"),
            new SheetUserData("Sales Data", "Charlie", "3 MB"),
            new SheetUserData("Employee Records", "Alice", "4 MB"),
            new SheetUserData("Inventory", "Bob", "2.5 MB"),
            new SheetUserData("Marketing Plan", "Charlie", "1 MB"),
            new SheetUserData("Customer Data", "Alice", "3.5 MB"),
            new SheetUserData("Product Catalog", "Bob", "2 MB"),
            new SheetUserData("Financials", "Charlie", "1.5 MB"),
            new SheetUserData("HR Records", "Alice", "2 MB"),
            new SheetUserData("Project Plan", "Bob", "1.5 MB"),
            new SheetUserData("Sales Data", "Charlie", "3 MB"),
            new SheetUserData("Employee Records", "Alice", "4 MB"),
            new SheetUserData("Inventory", "Bob", "2.5 MB")
    );

    ObservableList<PermissionUserData> permissions = FXCollections.observableArrayList(
            new PermissionUserData("Alice", "Read", "Granted"),
            new PermissionUserData("Bob", "Edit", "Pending"),
            new PermissionUserData("Charlie", "Admin", "Denied"),
            new PermissionUserData("Alice", "Read", "Granted"),
            new PermissionUserData("Bob", "Edit", "Pending"),
            new PermissionUserData("Charlie", "Admin", "Denied"),
            new PermissionUserData("Alice", "Read", "Granted"),
            new PermissionUserData("Bob", "Edit", "Pending"),
            new PermissionUserData("Charlie", "Admin", "Denied"),
            new PermissionUserData("Alice", "Read", "Granted"),
            new PermissionUserData("Bob", "Edit", "Pending"),
            new PermissionUserData("Charlie", "Admin", "Denied"),
            new PermissionUserData("Alice", "Read", "Granted"),
            new PermissionUserData("Bob", "Edit", "Pending"),
            new PermissionUserData("Charlie", "Admin", "Denied"),
            new PermissionUserData("Alice", "Read", "Granted"),
            new PermissionUserData("Bob", "Edit", "Pending"),
            new PermissionUserData("Charlie", "Admin", "Denied"),
            new PermissionUserData("Alice", "Read", "Granted"),
            new PermissionUserData("Bob", "Edit", "Pending"),
            new PermissionUserData("Charlie", "Admin", "Denied"),
            new PermissionUserData("Alice", "Read", "Granted"),
            new PermissionUserData("Bob", "Edit", "Pending"),
            new PermissionUserData("Charlie", "Admin", "Denied")
    );

    availableSheetsTable.setItems(sheets);
    permissionsTable.setItems(permissions);
}

}
