package com.sheetcell.engine.utils;

import com.sheetcell.engine.expression.parser.FunctionParser;
import com.sheetcell.engine.sheet.Sheet;

public class XMLSheetProcessorTest {

    public static void main(String[] args) {
        XMLSheetProcessorTest test = new XMLSheetProcessorTest();
//        test.testProcessAdvancedXMLFile();
        test.errortestProcessAdvancedXMLFile();
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

    public void errortestProcessAdvancedXMLFile() {
        XMLSheetProcessor processor = new XMLSheetProcessor();
        String filePath = "C:/Users/ahmad/Downloads/error-4.xml"; // Assuming you save the error-xml as basic.xml in this location

        try {
            processor.processSheetFile(filePath);
            Sheet sheet = processor.getCurrentSheet();

            // Verify sheet name
            assertCondition("beginner".equals(sheet.getName()), "Sheet name should be 'beginner'");

            // Verify sheet dimensions
            assertCondition(sheet.getMaxRows() == 3, "Sheet should have 3 rows");
            assertCondition(sheet.getMaxColumns() == 5, "Sheet should have 5 columns");

            // Verify cell contents
            assertCondition("Hello".equals(sheet.getOriginalValue(1, 1)), "Cell B2 should contain 'Hello'");
            assertCondition("Menash".equals(sheet.getOriginalValue(1, 2)), "Cell C2 should contain 'Menash'");
            assertCondition("5".equals(sheet.getOriginalValue(0, 0)), "Cell A1 should contain '5'");
            assertCondition("{CONCAT,{REF,A1},{REF,C2}}".equals(sheet.getOriginalValue(1, 3)), "Cell D2 should contain '{CONCAT,{REF,A1},{REF,C2}}'");

            // Calculate effective values for cells
            sheet.getCell(1, 1).calculateEffectiveValue();
            sheet.getCell(1, 2).calculateEffectiveValue();
            sheet.getCell(0, 0).calculateEffectiveValue();
            //sheet.getCell(1, 3).calculateEffectiveValue();


            // Calculate and verify the effective value of cell D2
            System.out.println("Cell D2 effective value: " + sheet.getCell(1, 3).calculateEffectiveValue());

            System.out.println("All tests passed successfully.");

        } catch (Exception e) {
            System.out.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void assertCondition(boolean condition, String errorMessage) {
        if (!condition) {
            throw new AssertionError(errorMessage);
        }
    }
}
