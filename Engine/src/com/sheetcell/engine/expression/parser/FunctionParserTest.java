package com.sheetcell.engine.expression.parser;

import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;

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
        assertResult("helloworld", result.getValue(), CellType.STRING, result.getCellType(), "testConcatFunction");
    }

    private static void testSubFunction() {
        String input = "{sub, hello world, 0, 4}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult("hello", result.getValue(), CellType.STRING, result.getCellType(), "testSubFunction");
    }

    private static void testNestedFunctions() {
        // Test nested PLUS and TIMES
        String input = "{plus, {times, 3, 4}, {minus, 10, 2}}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null, null);
        assertResult(20.0, result.getValue(), CellType.NUMERIC, result.getCellType(), "testNestedFunctions");

        // Test nested SUB and CONCAT
        input = "{concat, {sub, helloworld, 0, 4}, {sub, helloworld, 5, 9}}";
        expression = FunctionParser.parseExpression(input);
        result = expression.eval(null, null);
        assertResult("helloworld", result.getValue(), CellType.STRING, result.getCellType(), "testNestedFunctions - Sub and Concat");
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
}


