package com.sheetcell.engine.utils;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.expression.parser.FunctionParser;
import com.sheetcell.engine.sheet.Sheet;

public class XMLSheetProcessorTest {

    public static void main(String[] args) {
        XMLSheetProcessorTest test = new XMLSheetProcessorTest();
       test.advancedtestProcessAdvancedXMLFile();
        test.testProcessError4XMLFile();
        test.testProcessBasicConcatXMLFile();
        test.testProcessSheetError2();
        test.testProcessCarInsuranceXMLFile();
    }
    public static void testProcessCarInsuranceXMLFile() {
        XMLSheetProcessor processor = new XMLSheetProcessor();
        String filePath = "C:/Users/ahmad/Downloads/insurance.xml"; // Update this with the actual path to your XML file

        try {
            // Process the XML file
            processor.processSheetFile(filePath);
            Sheet sheet = processor.getCurrentSheet();

            // Check the values in the sheet

            // B4 = "AIG"
            Cell cellB4 = sheet.getCell(3, 1); // Row 4, Column B (1-based to 0-based index)
            assertEqual("AIG", cellB4.getEffectiveValue().getValue());

            // C4 = 1000
            Cell cellC4 = sheet.getCell(3, 2); // Row 4, Column C (1-based to 0-based index)
            assertEqual(1000.0, cellC4.getEffectiveValue().getValue());

            // D4 = 800
            Cell cellD4 = sheet.getCell(3, 3); // Row 4, Column D (1-based to 0-based index)
            assertEqual(800.0, cellD4.getEffectiveValue().getValue());

            // E4 = {PLUS, {REF,C4}, {REF,D4}} = 1800
            Cell cellE4 = sheet.getCell(3, 4); // Row 4, Column E (1-based to 0-based index)
            assertEqual(1800.0, cellE4.getEffectiveValue().getValue());

            // B5 = "WOBI"
            Cell cellB5 = sheet.getCell(4, 1); // Row 5, Column B (1-based to 0-based index)
            assertEqual("WOBI", cellB5.getEffectiveValue().getValue());

            // C5 = 900
            Cell cellC5 = sheet.getCell(4, 2); // Row 5, Column C (1-based to 0-based index)
            assertEqual(900.0, cellC5.getEffectiveValue().getValue());

            // D5 = 1100
            Cell cellD5 = sheet.getCell(4, 3); // Row 5, Column D (1-based to 0-based index)
            assertEqual(1100.0, cellD5.getEffectiveValue().getValue());

            // E5 = {PLUS, {REF,C5}, {REF,D5}} = 2000
            Cell cellE5 = sheet.getCell(4, 4); // Row 5, Column E (1-based to 0-based index)
            assertEqual(2000.0, cellE5.getEffectiveValue().getValue());

            // B6 = "SHIRBIT"
            Cell cellB6 = sheet.getCell(5, 1); // Row 6, Column B (1-based to 0-based index)
            assertEqual("SHIRBIT", cellB6.getEffectiveValue().getValue());

            // C6 = 5555
            Cell cellC6 = sheet.getCell(5, 2); // Row 6, Column C (1-based to 0-based index)
            assertEqual(5555.0, cellC6.getEffectiveValue().getValue());

            // D6 = 4444
            Cell cellD6 = sheet.getCell(5, 3); // Row 6, Column D (1-based to 0-based index)
            assertEqual(4444.0, cellD6.getEffectiveValue().getValue());

            // E6 = {PLUS, {REF,C6}, {REF,D6}} = 9999
            Cell cellE6 = sheet.getCell(5, 4); // Row 6, Column E (1-based to 0-based index)
            assertEqual(9999.0, cellE6.getEffectiveValue().getValue());

            System.out.println("All tests passed successfully.");

            // Print the effective values of the cells
            System.out.println("Cell B4 effective value: " + cellB4.getEffectiveValue().getValue());
            System.out.println("Cell C4 effective value: " + cellC4.getEffectiveValue().getValue());
            System.out.println("Cell D4 effective value: " + cellD4.getEffectiveValue().getValue());
            System.out.println("Cell E4 effective value: " + cellE4.getEffectiveValue().getValue());
            System.out.println("Cell B5 effective value: " + cellB5.getEffectiveValue().getValue());
            System.out.println("Cell C5 effective value: " + cellC5.getEffectiveValue().getValue());
            System.out.println("Cell D5 effective value: " + cellD5.getEffectiveValue().getValue());
            System.out.println("Cell E5 effective value: " + cellE5.getEffectiveValue().getValue());
            System.out.println("Cell B6 effective value: " + cellB6.getEffectiveValue().getValue());
            System.out.println("Cell C6 effective value: " + cellC6.getEffectiveValue().getValue());
            System.out.println("Cell D6 effective value: " + cellD6.getEffectiveValue().getValue());
            System.out.println("Cell E6 effective value: " + cellE6.getEffectiveValue().getValue());

        } catch (Exception e) {
            System.out.println("Processing XML file failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testProcessSheetError2() {
        XMLSheetProcessor processor = new XMLSheetProcessor();
        String filePath = "C:/Users/ahmad/Downloads/error-2.xml"; // Update this with the actual path to your XML file

        try {
            // Process the XML file, expecting an exception due to too many rows
            processor.processSheetFile(filePath);
            fail("Expected an IllegalArgumentException due to too many rows, but none was thrown.");
        }
        catch (Exception e) {
            System.out.println("Processing XML file fail due to: " + e.getMessage());
            System.out.println("Cancel the sheet ");
            //e.printStackTrace();
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
            assertEqual("Advanced Sheet", sheet.getSheetName());

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
            // Process the XML file, expecting an exception due to the incorrect CONCAT usage
            processor.processSheetFile(filePath);
            fail("Expected an IllegalArgumentException due to incorrect CONCAT function usage, but none was thrown.");
        }
         catch (Exception e) {
            System.out.println("Processing XML file failed with unexpected exception: " + e.getMessage());
            //e.printStackTrace();
            System.out.println("Cancel the sheet ");
        }
    }

    public static void testProcessBasicConcatXMLFile() {
        XMLSheetProcessor processor = new XMLSheetProcessor();
        String filePath = "C:/Users/ahmad/Downloads/basic.xml"; // Update this with the actual path to your XML file

        try {
            // Process the XML file
            processor.processSheetFile(filePath);
            Sheet sheet = processor.getCurrentSheet();

            // Assert the sheet name
            assertEqual("beginner", sheet.getSheetName());

            // Check the value of cell B2 (should be "Hello")
            Cell cellB2 = sheet.getCell(1, 1); // Row 2, Column B (1-based to 0-based index)
            assertEqual("Hello", cellB2.getEffectiveValue().getValue());

            // Check the value of cell C2 (should be "Menash")
            Cell cellC2 = sheet.getCell(1, 2); // Row 2, Column C (1-based to 0-based index)
            assertEqual("Menash", cellC2.getEffectiveValue().getValue());

            // Check the value of cell D2 (should be "HelloMenash" as a result of CONCAT)
            Cell cellD2 = sheet.getCell(1, 3); // Row 2, Column D (1-based to 0-based index)
            assertEqual("HelloMenash", cellD2.getEffectiveValue().getValue());

            System.out.println("All tests passed successfully.");
        } catch (Exception e) {
            System.out.println("Processing XML file failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // The assertEqual method for comparing values
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

    private static void fail(String message) {
        throw new AssertionError(message);
    }

    private void assertCondition(boolean condition, String errorMessage) {
        if (!condition) {
            throw new AssertionError(errorMessage);
        }
    }
}
