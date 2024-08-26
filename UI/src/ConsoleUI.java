import com.sheetcell.engine.Engine;
import com.sheetcell.engine.EngineImpl;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.sheet.api.SheetReadActions;
import com.sheetcell.engine.cell.Cell;

public class ConsoleUI {
    private final Engine engine;
    SheetReadActions sheet;

    public ConsoleUI() {
        this.engine = new EngineImpl();
       this.sheet = engine.getReadOnlySheet();
    }

    void displaySheet() {
        this.sheet = engine.getReadOnlySheet(); // Refresh to the latest sheet version

        // Display the sheet version
        System.out.println("Sheet Version: " + sheet.getVersion());

        // Display the sheet name if available
        String sheetName = sheet.getSheetName(); // Placeholder for getting the sheet name
        if (sheetName != null && !sheetName.isEmpty()) {
            System.out.println("Sheet Name: " + sheetName);
        }

        // Display column headers
        printColumnHeaders();

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

    private void printColumnHeaders() {
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

    // This could be a configurable precision, for now let's assume 2 decimal places
    private int getNumericPrecision() {
        return 2;
    }

    // **************************************method2******************************************

    public void displaySingleCellDetails(int row, int column) {
        // Validate the row and column
        if (row < 0 || row >= sheet.getMaxRows() || column < 0 || column >= sheet.getMaxColumns()) {
            System.out.println("Invalid cell coordinates.");
            return;
        }
        // Retrieve the cell
        Cell cell = sheet.getCell(row, column);
        if (cell == null) {
            System.out.println("Cell not found.");
            return;
        }

        // Display the details
        System.out.println("Cell Identity: " + CoordinateFactory.convertIndexToCellCord(row, column));
        System.out.println("Original Value: " + cell.getOriginalValue());
        System.out.println("Effective Value: " + (cell.getEffectiveValue() != null ? cell.getEffectiveValue().getValue() : "null"));
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
            cell.getInfluencedCells().forEach(inf -> System.out.print(inf.getCoordinate()));
            System.out.println();
        }
    }
    // **************************************method3******************************************

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
            System.out.println("Cell " + cellId + " not found in the sheet.");
        }
    }

    public void updateCell(String cellId,String newValue) {
        try {
            // Step 1: Validate cell ID
            // Step 2: Display original and effective value
            displayCellInfo(cellId);

            // Step 3: Prompt user for new value

            // Step 4: Update the cell and process recalculations
            engine.setCellValue(cellId, newValue);


        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            // Optionally, retry or ask for input again
        }
        finally {
            displaySheet();
        }
    }

    public static void main(String[] args) {
        ConsoleUI consoleUI = new ConsoleUI();
        try {
            String filePath = "C:/Users/ahmad/Downloads/Advanced.xml"; // Update this with the actual path to your XML file
            consoleUI.engine.loadSheet(filePath);
            consoleUI.displaySheet();
            int initialVersion = consoleUI.engine.getReadOnlySheet().getVersion();
            consoleUI.updateCell("A1", "Updated Value");
            consoleUI.updateCell("D2", "123.45");
            int newVersion = consoleUI.engine.getReadOnlySheet().getVersion();
            System.out.println("Initial Version: " + initialVersion + ", New Version: " + newVersion);
            consoleUI.updateCell("E4", "true");
            consoleUI.updateCell("D5","{plus,{ref,d4},-4}");


        } catch (Exception e) {
            System.out.println("Failed to load sheet: " + e.getMessage());
        }
    }
}
