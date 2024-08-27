import com.sheetcell.engine.Engine;
import com.sheetcell.engine.EngineImpl;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.utils.SheetUpdateResult;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class ConsoleUI {
    private Engine engine;
    private boolean isSheetLoaded = false;

    public ConsoleUI() {
        this.engine = new EngineImpl();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleLoadSheet();
                    break;
                case "2":
                    if (isSheetLoaded) {
                        handleDisplaySheet();
                    } else {
                        System.out.println("Error: No sheet is loaded. Please load a sheet first (Option 1).");
                    }
                    break;
                case "3":
                    if (isSheetLoaded) {
                        handleDisplaySingleCell();
                    } else {
                        System.out.println("Error: No sheet is loaded. Please load a sheet first (Option 1).");
                    }
                    break;
                case "4":
                    if (isSheetLoaded) {
                        handleUpdateCell();
                    } else {
                        System.out.println("Error: No sheet is loaded. Please load a sheet first (Option 1).");
                    }
                    break;
                case "5":
                    if (isSheetLoaded) {
                        handleDisplayVersions();
                    } else {
                        System.out.println("Error: No sheet is loaded. Please load a sheet first (Option 1).");
                    }
                    break;
                case "6":
                    running = false;
                    System.out.println("Exiting the system...");
                    break;
                case "7":
                    handleSaveLoadSystemState(isSheetLoaded);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Load Sheet from XML File");
        System.out.println("2. Display Current Sheet");
        System.out.println("3. Display Single Cell Details");
        System.out.println("4. Update Cell Value");
        System.out.println("5. Display Versions");
        System.out.println("6. Exit");
        System.out.println("7. Save/Load System State");
        System.out.print("Please select an option: ");
    }

    private void handleLoadSheet() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the full path to the XML file: ");
        String filePath = scanner.nextLine().trim();

        try {
            // Attempt to load the sheet
            engine.loadSheet(filePath);
            isSheetLoaded = true;
            System.out.println("Sheet loaded successfully.");
            displaySheet(engine.getReadOnlySheet());

        } catch (Exception e) {
            System.out.println("Failed to load the sheet. Error: " + e.getMessage());
        }
    }

    private void handleDisplaySheet() {
        try {
            System.out.println("Displaying the current sheet...");
            displaySheet(engine.getReadOnlySheet());
        } catch (Exception e) {
            System.out.println("Error displaying the sheet: " + e.getMessage());
        }
    }

    private void handleDisplaySingleCell() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the cell identifier (e.g., A1): ");
        String cellId = scanner.nextLine().trim();

        try {
            // Attempt to display the single cell details
            displaySingleCellDetails(engine.getReadOnlySheet(), cellId);
        } catch (IllegalArgumentException e) {
            // Handle any errors that occur during cell validation or retrieval
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other exceptions and provide a general error message
            System.out.println("Error displaying cell details: " + e.getMessage());
        }
    }

    private void handleUpdateCell() {
        Scanner scanner = new Scanner(System.in);
        boolean updatedSuccessfully = false;

        while (!updatedSuccessfully) {
            try {
                System.out.print("Enter the cell identifier (e.g., A1): ");
                String cellId = scanner.nextLine().trim();

                // Step 1: Validate the cell ID using the engine method
                engine.doesCellIdVaild(cellId);

                // Step 2: Display original and effective value
                displayCellInfo(cellId);

                // Step 3: Prompt user for new value
                System.out.print("Enter the new value for the cell (or leave empty to clear the cell): ");
                String newValue = scanner.nextLine().trim();

                // Step 4: Update the cell and process recalculations
                SheetUpdateResult result = engine.setCellValue(cellId, newValue);
                if (result.isNoActionNeeded()) {
                    System.out.println("The cell at " + cellId + " was already empty. No action was needed.");
                    updatedSuccessfully = true;
                    displaySheet(engine.getReadOnlySheet());
                } else if (result.hasError()) {
                    System.out.println("Update failed: " + result.getErrorMessage());
                } else {
                    System.out.println("Cell " + cellId + " updated successfully.");
                    updatedSuccessfully = true;
                    displaySheet(engine.getReadOnlySheet());

                }

            } catch (IllegalArgumentException e) {
                // Handle any validation errors from doesCellIdVaild or other checks
                System.out.println("Update failed: " + e.getMessage());
                System.out.println("Please try again.");
            } catch (Exception e) {
                // Catch any other exceptions and provide a general error message
                System.out.println("Error occurred while updating the cell" + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    private void handleDisplayVersions() {
        try {
            // Step 1: Get the available versions from the engine
            Map<Integer, Integer> versions = engine.getSheetVersions();

            // Step 2: Display the versions in a table format
            System.out.println("Available Versions:");
            System.out.println("Version | Cells Changed");
            System.out.println("--------|--------------");
            for (Map.Entry<Integer, Integer> entry : versions.entrySet()) {
                System.out.printf("%7d | %13d\n", entry.getKey(), entry.getValue());
            }

            // Step 3: Prompt the user to select a version for viewing
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the version number you wish to view: ");
            String input = scanner.nextLine().trim();

            try {
                int version = Integer.parseInt(input);

                // Step 4: Validate the version number and display the corresponding sheet version
                SheetReadActions versionSheet = engine.getSheetVersion(version);
                System.out.println("Displaying sheet for version: " + version);
                displaySheet(versionSheet);

            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Error occurred while displaying versions: "+ e.getMessage());
        }
    }

    private void handleSaveLoadSystemState(boolean isSheetLoaded) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Would you like to save (S) or load (L) the system state?");
        String choice = scanner.nextLine().trim().toUpperCase();

        if (choice.equals("S")) {
            if (!isSheetLoaded) {
                System.out.println("Error: No sheet is loaded. Please load a sheet to save the system state.");
                return;
            }
            System.out.print("Enter the full file path where you want to save the system state: ");
            String filePath = scanner.nextLine().trim();
            if (!filePath.endsWith(".dat")) {
                filePath += ".dat"; // Ensure the correct extension
            }
            try {
                engine.saveSheet(filePath);
                System.out.println("System state saved successfully to " + filePath);
            } catch (IOException e) {
                System.out.println("Failed to save the system state: " + e.getMessage());
            }
        } else if (choice.equals("L")) {
            System.out.print("Enter the full file path from which you want to load the system state: ");
            String filePath = scanner.nextLine().trim();
            if (!filePath.endsWith(".dat")) {
                filePath += ".dat"; // Ensure the correct extension
            }
                EngineImpl loadedEngine = EngineImpl.loadState(filePath);
                if (loadedEngine != null) {
                    System.out.println("System state loaded successfully from " + filePath);
                    engine = loadedEngine; // Replace the current engine with the loaded one
                    displaySheet(engine.getReadOnlySheet()); // Display the loaded sheet
                } else {
                    System.out.println("Failed to load the system state from the specified file.");
                }
        } else {
            System.out.println("Invalid choice. Please enter 'S' to save or 'L' to load.");
        }
    }



    //********************************************************************************************************
    public void setNewEngine(Engine engine){
        this.engine= engine;
    }
    //********************************************************************************************************
    void displaySheet(SheetReadActions sheet) {

        // Display the sheet version
        System.out.println("Sheet Version: " + sheet.getVersion());

        // Display the sheet name if available
        String sheetName = sheet.getSheetName(); // Placeholder for getting the sheet name
        if (sheetName != null && !sheetName.isEmpty()) {
            System.out.println("Sheet Name: " + sheetName);
        }

        // Display column headers
        printColumnHeaders(sheet);

        // Display the sheet content row by row
        for (int row = 0; row < sheet.getMaxRows(); row++) {
            // Print row number
            System.out.printf("%02d ", row + 1); // Print row number with leading zeroes

            for (int col = 0; col < sheet.getMaxColumns(); col++) {
                Cell cell = sheet.getCell(row, col);
                String cellContent = formatCellContent(cell);
                System.out.printf("%-" + sheet.getColumnWidth() + "s|", cellContent);
            }
            System.out.println(); // Move to the next line after each row
        }
    }

    private void printColumnHeaders(SheetReadActions sheet) {
        System.out.print("   "); // Adjust for row number column

        for (int col = 0; col < sheet.getMaxColumns(); col++) {
            char columnLetter = (char) ('A' + col); // Convert column number to a letter
            System.out.printf("%-" + sheet.getColumnWidth() + "s|", columnLetter);
        }
        System.out.println();
    }

    private String formatCellContent(Cell cell) {
        if (cell == null || cell.getEffectiveValue() == null || cell.getEffectiveValue().getValue() == null) {
            return ""; // Return empty string for empty cells
        }

        EffectiveValue effectiveValue = cell.getEffectiveValue();
        Object value = effectiveValue.getValue();

        // Determine how to display the value based on its type
        if (effectiveValue.getCellType() == CellType.NUMERIC) {
            return String.format("%." + getNumericPrecision() + "f", (Double) value);
        } else if (effectiveValue.getCellType() == CellType.STRING) {
            return value.toString();
        } else if (effectiveValue.getCellType() == CellType.BOOLEAN) {
            return value.toString().toUpperCase(); // Boolean values in uppercase
        } else {
            return ""; // For unknown or unsupported types, return an empty string
        }
    }

    // 2 decimal places(For example 0.00)
    private int getNumericPrecision() {
        return 2;
    }
    //********************************************************************************************************

    public void displaySingleCellDetails(SheetReadActions sheet, String cellId) throws IllegalArgumentException {
        // Validate and check the existence of the cell using the engine method
        engine.doesCellIdVaildAndExist(cellId);

        // Convert the cell ID to row and column indices
        int[] coords = CoordinateFactory.convertCellIdToIndex(cellId);
        int row = coords[0];
        int column = coords[1];

        // Retrieve the cell (since it exists, we are safe to do so)
        Cell cell = sheet.getCell(row, column);

        // Display the details
        System.out.println("Cell Details:");
        System.out.println("-------------");
        System.out.println("Cell Identifier: " + cellId);
        System.out.println("Original Value: " + (cell.getOriginalValue() != null ? cell.getOriginalValue() : "No original value set"));
        System.out.println("Effective Value: " + (cell.getEffectiveValue() != null ? cell.getEffectiveValue().getValue() : "No effective value computed"));
        System.out.println("Last Modified Version: " + cell.getVersion());

        // Display dependencies
        System.out.print("Depends on cells: ");
        if (cell.getDependencies().isEmpty()) {
            System.out.println("None");
        } else {
            cell.getDependencies().forEach(dep -> System.out.print(dep.getCoordinate() + " "));
            System.out.println();
        }

        // Display influenced cells
        System.out.print("Influences cells: ");
        if (cell.getInfluencedCells().isEmpty()) {
            System.out.println("None");
        } else {
            cell.getInfluencedCells().forEach(inf -> System.out.print(inf.getCoordinate() + " "));
            System.out.println();
        }
    }

    //********************************************************************************************************
    private void displayCellInfo(String cellId) {
        int[] coords= CoordinateFactory.convertCellIdToIndex(cellId);
        int row= coords[0];
        int col= coords[1];
        Cell cell = engine.getReadOnlySheet().getCell(row, col);

        if (cell != null) {
            System.out.println("Cell Identifier: " + cellId);
            System.out.println("Original Value: " + cell.getOriginalValue());
            EffectiveValue effectiveValue = cell.getEffectiveValue();
            System.out.println("Effective Value: " + (effectiveValue != null ? effectiveValue.getValue() : "None"));
        } else {
            System.out.println("The cell " + cellId.toUpperCase() + " is currently empty. You can assign a new value to it.");
        }
    }

    //********************************************************************************************************
    public static void main(String[] args) {
        ConsoleUI consoleUI = new ConsoleUI();
        consoleUI.start();
    }
    //**************************************tests******************************************
//    public static void test() {
//        ConsoleUI consoleUI = new ConsoleUI();
//        try {
//            String filePath = "C:/Users/ahmad/Downloads/Advanced.xml"; // Update this with the actual path to your XML file
//            consoleUI.engine.loadSheet(filePath);
//            consoleUI.displaySheet(consoleUI.engine.getReadOnlySheet());
//            int initialVersion = consoleUI.engine.getReadOnlySheet().getVersion();
//            consoleUI.updateCell("A1", "Updated Value");
//            consoleUI.updateCell("D2", "123.45");
//            int newVersion = consoleUI.engine.getReadOnlySheet().getVersion();
//            System.out.println("Initial Version: " + initialVersion + ", New Version: " + newVersion);
//            consoleUI.updateCell("E4", "true");
//            consoleUI.updateCell("D5","{plus,10,-4}");
//            Map<Integer, Integer> versions = consoleUI.engine.getSheetVersions();
//            System.out.println("Available Sheet Versions:");
//            System.out.println("Version | Cells Changed");
//            System.out.println("--------|--------------");
//            for (Map.Entry<Integer, Integer> entry : versions.entrySet()) {
//                System.out.printf("%7d | %13d\n", entry.getKey(), entry.getValue());
//            }
//            // Prompt user to select a version to view
//            System.out.print("Enter version number to view (or press Enter to cancel): ");
//            String input = new Scanner(System.in).nextLine().trim();
//            if (!input.isEmpty()) {
//                try {
//                    int version = Integer.parseInt(input);
//                    SheetReadActions versionSheet = consoleUI.engine.getSheetVersion(version);
//                    consoleUI.displaySheet(versionSheet);
//                } catch (NumberFormatException e) {
//                    System.out.println("Invalid input. Please enter a valid number.");
//                } catch (IllegalArgumentException e) {
//                    System.out.println(e.getMessage());
//                }
//            }
//
//        } catch (Exception e) {
//            System.out.println("Failed to load sheet: " + e.getMessage());
//        }
//        try {
//            consoleUI.engine.saveSheet("C:\\Users\\ahmad\\OneDrive\\Desktop\\CS_Degree\\mySheet.dat");
//            Engine loadedEngine = EngineImpl.loadState("C:\\Users\\ahmad\\OneDrive\\Desktop\\CS_Degree\\mySheet.dat");
//            if (loadedEngine != null){
//                System.out.println("Sheet saved and loaded successfully.");
//                consoleUI.setNewEngine(loadedEngine);
//                consoleUI.displaySheet(consoleUI.engine.getReadOnlySheet());
//                consoleUI.updateCell("D5","{plus,10,-4}");
//                consoleUI.updateCell("c5","{plus,{ref,d5},-4}");
//            }
//            else {
//                System.out.println("Failed to load the saved sheet.");
//            }
//
//        } catch (IOException e) {
//            System.out.println("An error occurred while saving the sheet: " + e.getMessage());
//            // Additional handling can be done here, such as notifying the user or logging the error
//        }
//    }
}