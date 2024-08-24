package com.sheetcell.engine.utils;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.expression.parser.FunctionParser;
import com.sheetcell.engine.sheet.Sheet;

public class XMLSheetProcessorTest {

    public static void main(String[] args) {
        XMLSheetProcessorTest test = new XMLSheetProcessorTest();
//        test.testProcessAdvancedXMLFile();
     //   test.advancedtestProcessAdvancedXMLFile();
        test.testProcessError4XMLFile();
    }

    public void testProcessAdvancedXMLFile() {
        XMLSheetProcessor processor = new XMLSheetProcessor();
        String filePath = "C:/Users/ahmad/Downloads/error-4.xml"; // Make sure to place the advanced.xml in the correct path

        try {
            processor.processSheetFile(filePath);
            Sheet sheet = processor.getCurrentSheet();

            // Verify sheet name
            assertCondition("Car Insurance".equals(sheet.getName()), "Sheet name should be 'Car Insurance'");

            // Verify sheet dimensions
            assertCondition(sheet.getMaxRows() == 7, "Sheet should have 7 rows");
            assertCondition(sheet.getMaxColumns() == 7, "Sheet should have 7 columns");

            // Verify cell contents
            assertCondition("AIG".equals(sheet.getOriginalValue(3, 1)), "Cell B4 should contain 'AIG'");
            assertCondition("WOBI".equals(sheet.getOriginalValue(4, 1)), "Cell B5 should contain 'WOBI'");
            assertCondition("SHIRBIT".equals(sheet.getOriginalValue(5, 1)), "Cell B6 should contain 'SHIRBIT'");
            assertCondition("mandatory".equals(sheet.getOriginalValue(2, 2)), "Cell C3 should contain 'mandatory'");
            assertCondition("1000".equals(sheet.getOriginalValue(3, 2)), "Cell C4 should contain '1000'");
            assertCondition("900".equals(sheet.getOriginalValue(4, 2)), "Cell C5 should contain '900'");
            assertCondition("5555".equals(sheet.getOriginalValue(5, 2)), "Cell C6 should contain '5555'");
            assertCondition("3rd party".equals(sheet.getOriginalValue(2, 3)), "Cell D3 should contain '3rd party'");
            assertCondition("800".equals(sheet.getOriginalValue(3, 3)), "Cell D4 should contain '800'");
            assertCondition("1100".equals(sheet.getOriginalValue(4, 3)), "Cell D5 should contain '1100'");
            assertCondition("4444".equals(sheet.getOriginalValue(5, 3)), "Cell D6 should contain '4444'");
            assertCondition("Total".equals(sheet.getOriginalValue(2, 4)), "Cell E3 should contain 'Total'");
            assertCondition("{PLUS,{REF,C4},{REF,D4}}".equals(sheet.getOriginalValue(3, 4)), "Cell E4 should contain '{PLUS,{REF,C4},{REF,D4}}'");
            assertCondition("{PLUS,{REF,C5},{REF,D5}}".equals(sheet.getOriginalValue(4, 4)), "Cell E5 should contain '{PLUS,{REF,C5},{REF,D5}}'");
            assertCondition("{PLUS,{REF,C6},{REF,D6}}".equals(sheet.getOriginalValue(5, 4)), "Cell E6 should contain '{PLUS,{REF,C6},{REF,D6}}'");
            // Calculate effective values for cells
            sheet.getCell(3,1).calculateEffectiveValue();
            sheet.getCell(4,1).calculateEffectiveValue();
            sheet.getCell(5,1).calculateEffectiveValue();
            sheet.getCell(2,2).calculateEffectiveValue();
            sheet.getCell(3,2).calculateEffectiveValue();
            sheet.getCell(4,2).calculateEffectiveValue();
            sheet.getCell(5,2).calculateEffectiveValue();
            sheet.getCell(2,3).calculateEffectiveValue();
            sheet.getCell(3,3).calculateEffectiveValue();
            sheet.getCell(4,3).calculateEffectiveValue();
            sheet.getCell(5,3).calculateEffectiveValue();
            sheet.getCell(2,4).calculateEffectiveValue();
            sheet.getCell(3,4).calculateEffectiveValue();
            sheet.getCell(4,4).calculateEffectiveValue();
            sheet.getCell(5,4).calculateEffectiveValue();

            // Calculate effective values for formulas
            System.out.println("All tests passed successfully.");
            System.out.println("Sheet name: " + sheet.getName());
            System.out.println("Sheet dimensions: " + sheet.getMaxRows() + " rows x " + sheet.getMaxColumns() + " columns");

            System.out.println("Cell B4 value: " + sheet.getOriginalValue(3, 1));
            System.out.println("Cell C4 value: " + sheet.getOriginalValue(3, 2));
            System.out.println("Cell D4 value: " + sheet.getOriginalValue(3, 3));
            System.out.println("Cell E4 formula: " + sheet.getOriginalValue(3, 4));
            System.out.println("Cell E4 effective value: " + sheet.getCell(3, 4).getEffectiveValue().toString());

            System.out.println("Cell E5 formula: " + sheet.getOriginalValue(4, 4));
            System.out.println("Cell E5 effective value: " + sheet.getCell(4, 4).getEffectiveValue());

            System.out.println("Cell E6 formula: " + sheet.getOriginalValue(5, 4));
            System.out.println("Cell E6 effective value: " + sheet.getCell(5, 4).getEffectiveValue());

        } catch (Exception e) {
            System.out.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void advancedtestProcessAdvancedXMLFile() {
        XMLSheetProcessor processor = new XMLSheetProcessor();
        String filePath = "C:/Users/ahmad/Downloads/advanced.xml"; // Ensure this is the correct path

        try {
            // Process the XML file
            processor.processSheetFile(filePath);
            Sheet sheet = processor.getCurrentSheet();

            // Validate the sheet name
            assertEqual("Advanced Sheet", sheet.getName());

            // Validate some specific cells
            assertEqual("Test", sheet.getCell(0, 0).getEffectiveValue());
            assertEqual("Value", sheet.getCell(1, 0).getEffectiveValue());
            assertEqual(100.0, sheet.getCell(0, 1).getEffectiveValue());
            assertEqual(200.0, sheet.getCell(1, 1).getEffectiveValue());

            // Validate formula results
            assertEqual(300.0, sheet.getCell(2, 1).getEffectiveValue()); // {PLUS,{REF,B1},{REF,B2}} = 100 + 200 = 300
            assertEqual("TestValue", sheet.getCell(2, 0).getEffectiveValue()); // {CONCAT,{REF,A1},{REF,A2}} = "Test" + "Value" = "TestValue"
            assertEqual(600.0, sheet.getCell(3, 1).getEffectiveValue()); // {times,{REF,B3},2} = 300 * 2 = 600
            assertEqual(4.0, sheet.getCell(1, 2).getEffectiveValue()); // {DIVIDE,{REF,B2},{REF,C1}} = 200 / 50 = 4

            System.out.println("All assertions passed!");

            // print the effective values of the cells
            System.out.println("Cell A1 effective value: " + sheet.getCell(0, 0).getEffectiveValue());
            System.out.println("Cell A2 effective value: " + sheet.getCell(1, 0).getEffectiveValue());
            System.out.println("Cell A3 effective value: " + sheet.getCell(2, 0).getEffectiveValue());
            System.out.println("Cell B1 effective value: " + sheet.getCell(0, 1).getEffectiveValue());
            System.out.println("Cell B2 effective value: " + sheet.getCell(1, 1).getEffectiveValue());
            System.out.println("Cell B3 effective value: " + sheet.getCell(2, 1).getEffectiveValue());
            System.out.println("Cell B4 effective value: " + sheet.getCell(3, 1).getEffectiveValue());
            System.out.println("Cell C1 effective value: " + sheet.getCell(0, 2).getEffectiveValue());
            System.out.println("Cell C2 effective value: " + sheet.getCell(1, 2).getEffectiveValue());



        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Processing XML file failed with exception: " + e.getMessage());
        }
    }

    public static void testProcessError4XMLFile() {
        XMLSheetProcessor processor = new XMLSheetProcessor();
        String filePath = "C:/Users/ahmad/Downloads/error-4.xml"; // Update this with the actual path to your XML file

        try {
            // Process the XML file
            processor.processSheetFile(filePath);
            Sheet sheet = processor.getCurrentSheet();

            // Assert the sheet name
            assertEqual("beginner", sheet.getName());

            // Check the value of cell B2 (should be "Hello")
            Cell cellB2 = sheet.getCell(1, 1); // Row 2, Column B (1-based to 0-based index)
            assertEqual("Hello", cellB2.getEffectiveValue().getValue());

            // Check the value of cell C2 (should be NaN due to division by zero)
            Cell cellC2 = sheet.getCell(1, 2); // Row 2, Column C (1-based to 0-based index)
            assertEqual(Double.NaN, cellC2.getEffectiveValue().getValue());

            // Check the value of cell A1 (should be 5)
            Cell cellA1 = sheet.getCell(0, 0); // Row 1, Column A (1-based to 0-based index)
            assertEqual(5.0, cellA1.getEffectiveValue().getValue());

            // Check the value of cell D2 (should be NaN due to reference to NaN in C2)
            Cell cellD2 = sheet.getCell(1, 3); // Row 2, Column D (1-based to 0-based index)
            assertEqual(Double.NaN, cellD2.getEffectiveValue().getValue());

            System.out.println("All tests passed successfully.");
        } catch (Exception e) {
            System.out.println("Processing XML file failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void assertEqual(Object expected, Object actual) {
        String expectedStr = expected != null ? expected.toString() : null;
        String actualStr = actual != null ? actual.toString() : null;

        if (expectedStr == null) {
            if (actualStr != null) {
                fail("Expected: null but got: '" + actualStr + "'");
            }
        } else if (!expectedStr.equals(actualStr)) {
            System.out.println("Character-by-character comparison:");
            for (int i = 0; i < Math.min(expectedStr.length(), actualStr.length()); i++) {
                System.out.println("Expected char " + i + ": " + (int) expectedStr.charAt(i));
                System.out.println("Actual char " + i + ": " + (int) actualStr.charAt(i));
            }
            fail("Expected: '" + expectedStr + "' but got: '" + actualStr + "'");
        }
    }

    // Helper method to fail with a custom message
    private static void fail(String message) {
        System.err.println("Assertion failed: " + message);
        throw new AssertionError(message);
    }

    private void assertCondition(boolean condition, String errorMessage) {
        if (!condition) {
            throw new AssertionError(errorMessage);
        }
    }
}
