package com.sheetcell.engine.expression.parser;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class FunctionParserTest {
    public static void main(String[] args) {
        // Test arithmetic functions
        testPlusFunction();
        testMinusFunction();
        testTimesFunction();
        testDivideFunction();
        testModFunction();
        testPowFunction();
        testAbsFunction();

        // Test string functions
        testConcatFunction();
        testSubFunction();

        // Test nested functions
        testNestedFunctions();

        // Test invalid cases
        testInvalidArguments();

        System.out.println("All level 1 tests passed successfully");
        System.out.println("Starting level 2 tests");
        System.out.println("*****************************************************************************************************************");
        // level 2 test cases
        testPlusFunctionWithTrimming();
        testPlusFunctionWithoutTrimming();
        testConcatFunctionWithSpaces();
        testConcatFunctionWithoutSpaces();
        testMinusFunctionWithNegativeResult();
        testModFunctionWithPositiveResult();
        testModFunctionWithNegativeDivisor();
        testSubFunctionWithString();
        testInvalidFunctionName();
        testNestedFunctionParsing();
        testSubFunctionWithSpacesInSource();
        testComplexNestedStringExpressions2();
        testComplexNestedStringExpressions3();

        // level 3 test cases
        System.out.println("starting level 3 tests");
        System.out.println("*****************************************************************************************************************");
        testComplexNestedNumericExpressions();
        testComplexNestedStringExpressions();
    }

    private static void testPlusFunction() {
        String input = "{plus, 3, 4}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(7.0, result.getValue(), CellType.NUMERIC, result.getCellType(), "testPlusFunction");
    }

    private static void testMinusFunction() {
        String input = "{minus, 10, 4}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(6.0, result.getValue(), CellType.NUMERIC, result.getCellType(), "testMinusFunction");
    }

    private static void testTimesFunction() {
        String input = "{times, 6, 7}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(42.0, result.getValue(), CellType.NUMERIC, result.getCellType(), "testTimesFunction");
    }

    private static void testDivideFunction() {
        String input = "{divide, 10, 2}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(5.0, result.getValue(), CellType.NUMERIC, result.getCellType(), "testDivideFunction");
    }

    private static void testModFunction() {
        String input = "{mod, 10, 3}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(1.0, result.getValue(), CellType.NUMERIC, result.getCellType(), "testModFunction");
    }

    private static void testPowFunction() {
        String input = "{pow, 2, 3}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(8.0, result.getValue(), CellType.NUMERIC, result.getCellType(), "testPowFunction");
    }

    private static void testAbsFunction() {
        String input = "{abs, -5}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(5.0, result.getValue(), CellType.NUMERIC, result.getCellType(), "testAbsFunction");
    }

    private static void testConcatFunction() {
        String input = "{concat, hello, world}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(" hello world", result.getValue(), CellType.STRING, result.getCellType(), "testConcatFunction");
    }

    private static void testSubFunction() {
        String input = "{sub, hello world, 0, 4}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(" hell", result.getValue(), CellType.STRING, result.getCellType(), "testSubFunction");
    }

    private static void testNestedFunctions() {
        // Test nested PLUS and TIMES
        String input = "{plus, {times, 3, 4}, {minus, 10, 2}}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(20.0, result.getValue(), CellType.NUMERIC, result.getCellType(), "testNestedFunctions");

        // Test nested SUB and CONCAT
        input = "{concat,{sub, helloworld,0,4},{sub, helloworld,5,9}}";
        expression = FunctionParser.parseExpression(input);
        result = expression.eval(null, null);
        assertResult(" helloworl", result.getValue(), CellType.STRING, result.getCellType(), "testNestedFunctions - Sub and Concat");
    }

    private static void testInvalidArguments() {
        try {
            String input = "{plus, 3}";
            FunctionParser.parseExpression(input);
            System.err.println("testInvalidArguments failed: Exception expected but not thrown for PLUS");
        } catch (IllegalArgumentException e) {
            System.out.println("testInvalidArguments passed for PLUS: " + e.getMessage());
        }

        try {
            String input = "{divide, 10, 0}";
            Expression expression = FunctionParser.parseExpression(input);
            EffectiveValue result = expression.eval(null, null);
            assertResult(Double.NaN, result.getValue(), CellType.NUMERIC, result.getCellType(), "testDivideByZero");
        } catch (IllegalArgumentException e) {
            System.err.println("testInvalidArguments failed: Exception should not be thrown for DIVIDE by zero");
        }

        try {
            String input = "{sub, helloworld, -1, 4}";
            Expression expression = FunctionParser.parseExpression(input);
            EffectiveValue result = expression.eval(null, null);
            assertResult("!UNDEFINED!", result.getValue(), CellType.STRING, result.getCellType(), "testSubWithOutOfBounds - negative start index");
        } catch (IllegalArgumentException e) {
            System.err.println("testInvalidArguments failed: Exception should not be thrown for SUB with out of bounds indices");
        }

        try {
            String input = "{times, hello, world}";
            FunctionParser.parseExpression(input);
            System.err.println("testInvalidArguments failed: Exception expected but not thrown for TIMES with strings");
        } catch (IllegalArgumentException e) {
            System.out.println("testInvalidArguments passed for TIMES with strings: " + e.getMessage());
        }
    }


    // Helper method for assertions
    private static void assertResult(Object expectedValue, Object actualValue, CellType expectedType, CellType actualType, String testName) {
        if (!expectedValue.equals(actualValue) || !expectedType.equals(actualType)) {
            throw new AssertionError(testName + " failed: Expected value " + expectedValue + " and type " + expectedType + " but got value " + actualValue + " and type " + actualType);
        } else {
            System.out.println(testName + " passed");
        }
    }

    // new test cases
    //*****************************************************************************************************************
    private static void assertEqual(Object expected, Object actual) {
        if (expected == null) {
            if (actual != null) {
                fail("Expected: null but got: '" + actual + "'");
            }
        } else if (!expected.equals(actual)) {
            fail("Expected: '" + expected + "' but got: '" + actual + "'");
        }
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }

    public static void testPlusFunctionWithTrimming() {
        String input = "{plus, 4 ,  5}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertEqual(9.0, result.getValue());
        assertEqual(CellType.NUMERIC, result.getCellType());
        System.out.println("testPlusFunctionWithTrimming passed");
    }

    public static void testPlusFunctionWithoutTrimming() {
        String input = "{plus,4,5}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertEqual(9.0, result.getValue());
        assertEqual(CellType.NUMERIC, result.getCellType());
        System.out.println("testPlusFunctionWithoutTrimming passed");
    }

    public static void testConcatFunctionWithSpaces() {
        String input = "{concat,hello ,  world}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertEqual("hello   world", result.getValue());
        assertEqual(CellType.STRING, result.getCellType());
        System.out.println("testConcatFunctionWithSpaces passed");
    }

    public static void testConcatFunctionWithoutSpaces() {
        String input = "{concat,hello,world}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertEqual("helloworld", result.getValue());
        assertEqual(CellType.STRING, result.getCellType());
        System.out.println("testConcatFunctionWithoutSpaces passed");
    }

    public static void testMinusFunctionWithNegativeResult() {
        String input = "{minus, 5, 10}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertEqual(-5.0, result.getValue());
        assertEqual(CellType.NUMERIC, result.getCellType());
        System.out.println("testMinusFunctionWithNegativeResult passed");
    }

    public static void testModFunctionWithPositiveResult() {
        String input = "{mod, 10, 3}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertEqual(1.0, result.getValue());
        assertEqual(CellType.NUMERIC, result.getCellType());
        System.out.println("testModFunctionWithPositiveResult passed");
    }

    public static void testModFunctionWithNegativeDivisor() {
        String input = "{mod, 10, -3}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertEqual(-2.0, result.getValue());
        assertEqual(CellType.NUMERIC, result.getCellType());
        System.out.println("testModFunctionWithNegativeDivisor passed");
    }

    public static void testSubFunctionWithString() {
        String input = "{sub,hello world, 0, 4}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertEqual("hello", result.getValue());
        assertEqual(CellType.STRING, result.getCellType());
        System.out.println("testSubFunctionWithString passed");
    }

    public static void testInvalidFunctionName() {
        String input = "{unknown, 4, 5}";
        try {
            FunctionParser.parseExpression(input);
            fail("Expected an IllegalArgumentException for an unknown function name");
        } catch (IllegalArgumentException e) {
            System.out.println("testInvalidFunctionName passed");
        }
    }

//    public static void testInvalidRefFunction() {
//        String input = "{ref, ZZ1000}";
//        try {
//            FunctionParser.parseExpression(input);
//            fail("Expected an IllegalArgumentException for an invalid cell reference");
//        } catch (IllegalArgumentException e) {
//            System.out.println("testInvalidRefFunction passed: " + e.getMessage());
//        }
//    }

    public static void testNestedFunctionParsing() {
        String input = "{plus,{minus,10,5},{times,2,3}}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertEqual(11.0, result.getValue());
        assertEqual(CellType.NUMERIC, result.getCellType());
        System.out.println("testNestedFunctionParsing passed");
    }

    public static void testSubFunctionWithSpacesInSource() {
        String input = "{sub,  hello world  , 0, 4}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertEqual("  hel", result.getValue());
        assertEqual(CellType.STRING, result.getCellType());
        System.out.println("testSubFunctionWithSpacesInSource passed");
    }

// level 3 test cases
//*****************************************************************************************************************

    public static void testComplexNestedNumericExpressions() {
        // Test input combining multiple numeric expressions
        String input = "{PLUS, {TIMES, {MOD, 17, 5}, {POW, 2, 3}}, {DIVIDE, {ABS, -20}, 4}}";

        try {
            Expression expression = FunctionParser.parseExpression(input);
            EffectiveValue result = expression.eval(null, null);

            // Breaking down the expression:
            // MOD(17, 5) = 2
            // POW(2, 3) = 8
            // TIMES(2, 8) = 16
            // ABS(-20) = 20
            // DIVIDE(20, 4) = 5
            // PLUS(16, 5) = 21
            Double expectedValue = 21.0;
            CellType expectedType = CellType.NUMERIC;

            assertResult(expectedValue, result.getValue(), expectedType, result.getCellType(), "testComplexNestedNumericExpressions");

        } catch (Exception e) {
            fail("testComplexNestedNumericExpressions failed with exception: " + e.getMessage());
        }
    }

    public static void testComplexNestedStringExpressions() {
        // Test input combining multiple string expressions
        String input = "{CONCAT,{SUB, HelloWorld, 0, 4},{SUB, WideWorld, 4, 9}}";

        try {
            Expression expression = FunctionParser.parseExpression(input);
            EffectiveValue result = expression.eval(null, null);

            // Breaking down the expression:
            // SUB(HelloWorld, 0, 4) = "Hello"
            // SUB(WideWorld, 4, 9) = "World"
            // CONCAT("Hello", "World") = "HelloWorld"
            String expectedValue = " HelleWorld";
            CellType expectedType = CellType.STRING;

            assertResult(expectedValue, result.getValue(), expectedType, result.getCellType(), "testComplexNestedStringExpressions");

        } catch (Exception e) {
            fail("testComplexNestedStringExpressions failed with exception: " + e.getMessage());
        }
    }

    public static void testComplexNestedStringExpressions2() {
        // Test input combining multiple string expressions
        String input = "{CONCAT,{SUB, spaceastro, 0, 4},{concat,e,{sub,  is the best, 0, 12}}}";

        try {
            Expression expression = FunctionParser.parseExpression(input);
            EffectiveValue result = expression.eval(null, null);


            String expectedValue = " space  is the best";
            CellType expectedType = CellType.STRING;

            assertResult(expectedValue, result.getValue(), expectedType, result.getCellType(), "testComplexNestedStringExpressions");

        } catch (Exception e) {
            fail("testComplexNestedStringExpressions failed with exception: " + e.getMessage());
        }
    }
    public static void testComplexNestedStringExpressions3() {
        // Test input combining multiple string expressions
        String input = "{SUB,{concat, hel, lo},0,6}";

        try {
            Expression expression = FunctionParser.parseExpression(input);
            EffectiveValue result = expression.eval(null, null);

            //{concat, hel, lo} = " hel lo"
            String expectedValue = " hel lo";
            CellType expectedType = CellType.STRING;

            assertResult(expectedValue, result.getValue(), expectedType, result.getCellType(), "testComplexNestedStringExpressions");

        } catch (Exception e) {
            fail("testComplexNestedStringExpressions failed with exception: " + e.getMessage());
        }
    }

}



