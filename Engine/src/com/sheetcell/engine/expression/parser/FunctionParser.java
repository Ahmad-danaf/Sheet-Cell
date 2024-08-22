package com.sheetcell.engine.expression.parser;

import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.expression.impl.*;
import com.sheetcell.engine.sheet.Sheet;
import com.sheetcell.engine.sheet.api.SheetReadActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public enum FunctionParser {
    IDENTITY {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for IDENTITY function. Expected 1, but got " + arguments.size());
            }
            // all is good. create the relevant function instance
            String actualValue = arguments.get(0).trim();
            if (isBoolean(actualValue)) {
                return new IdentityExpression(Boolean.parseBoolean(actualValue), CellType.BOOLEAN);
            } else if (isNumeric(actualValue)) {
                return new IdentityExpression(Double.parseDouble(actualValue), CellType.NUMERIC);
            } else {
                return new IdentityExpression(actualValue, CellType.STRING);
            }
        }

        private boolean isBoolean(String value) {
            return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
        }

        private boolean isNumeric(String value) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    },
    PLUS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function (e.g. number of arguments)
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for PLUS function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            // support UNKNOWN type as its value will be determined at runtime
            if ( (!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN)) ) {
                throw new IllegalArgumentException("Invalid argument types for PLUS function. Expected NUMERIC, but got " + leftCellType + " and " + rightCellType);
            }

            // all is good. create the relevant function instance
            return new PlusExpression(left, right);
        }
    },
    MINUS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for MINUS function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            if ( (!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for MINUS function. Expected NUMERIC, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            }

            // all is good. create the relevant function instance
            return new MinusExpression(left, right);
        }
    },
    DIVIDE {
        @Override
        public Expression parse(List<String> arguments) {
            // Ensure there are exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for DIVIDE function. Expected 2, but got " + arguments.size());
            }

            // Parse the arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // Validate that both arguments are numeric or unknown
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            if ((!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for DIVIDE function. Expected NUMERIC, but got " +
                        leftCellType + " and " + rightCellType);
            }

            // Create and return the DivideExpression
            return new DivideExpression(left, right);
        }
    },
    TIMES {
        @Override
        public Expression parse(List<String> arguments) {
            // Ensure there are exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for TIMES function. Expected 2, but got " + arguments.size());
            }

            // Parse the arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // Validate that both arguments are numeric or unknown
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            if ((!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for TIMES function. Expected NUMERIC, but got " +
                        leftCellType + " and " + rightCellType);
            }

            // Create and return the TimesExpression
            return new TimesExpression(left, right);
        }
    },
    MOD {
        @Override
        public Expression parse(List<String> arguments) {
            // Ensure there are exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for MOD function. Expected 2, but got " + arguments.size());
            }

            // Parse the arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // Validate that both arguments are numeric or unknown
            CellType leftCellType = left.getFunctionResultType();
            CellType rightCellType = right.getFunctionResultType();
            if ((!leftCellType.equals(CellType.NUMERIC) && !leftCellType.equals(CellType.UNKNOWN)) ||
                    (!rightCellType.equals(CellType.NUMERIC) && !rightCellType.equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for MOD function. Expected NUMERIC, but got " +
                        leftCellType + " and " + rightCellType);
            }

            // Create and return the ModExpression
            return new ModExpression(left, right);
        }
    },
    POW {
        @Override
        public Expression parse(List<String> arguments) {
            // Ensure there are exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for POW function. Expected 2, but got " + arguments.size());
            }

            // Parse the arguments
            Expression base = parseExpression(arguments.get(0).trim());
            Expression exponent = parseExpression(arguments.get(1).trim());

            // Validate that both arguments are numeric or unknown
            CellType baseCellType = base.getFunctionResultType();
            CellType exponentCellType = exponent.getFunctionResultType();
            if ((!baseCellType.equals(CellType.NUMERIC) && !baseCellType.equals(CellType.UNKNOWN)) ||
                    (!exponentCellType.equals(CellType.NUMERIC) && !exponentCellType.equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for POW function. Expected NUMERIC, but got " +
                        baseCellType + " and " + exponentCellType);
            }

            // Create and return the PowExpression
            return new PowExpression(base, exponent);
        }
    },
    ABS {
        @Override
        public Expression parse(List<String> arguments) {
            // Ensure there is exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for ABS function. Expected 1, but got " + arguments.size());
            }

            // Parse the argument
            Expression argument = parseExpression(arguments.get(0).trim());

            // Validate that the argument is numeric or unknown
            CellType argumentCellType = argument.getFunctionResultType();
            if (!argumentCellType.equals(CellType.NUMERIC) && !argumentCellType.equals(CellType.UNKNOWN)) {
                throw new IllegalArgumentException("Invalid argument type for ABS function. Expected NUMERIC, but got " + argumentCellType);
            }

            // Create and return the AbsExpression
            return new AbsExpression(argument);
        }
    },
    CONCAT {
        @Override
        public Expression parse(List<String> arguments) {
            // Ensure there are exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for CONCAT function. Expected 2, but got " + arguments.size());
            }

            // Parse the arguments
            Expression str1 = parseExpression(arguments.get(0).trim());
            Expression str2 = parseExpression(arguments.get(1).trim());

            // Validate that both arguments are strings or unknown
            CellType str1CellType = str1.getFunctionResultType();
            CellType str2CellType = str2.getFunctionResultType();
            if ((!str1CellType.equals(CellType.STRING) && !str1CellType.equals(CellType.UNKNOWN)) ||
                    (!str2CellType.equals(CellType.STRING) && !str2CellType.equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for CONCAT function. Expected STRING, but got " +
                        str1CellType + " and " + str2CellType);
            }

            // Create and return the ConcatExpression
            return new ConcatExpression(str1, str2);
        }
    },
    SUB {
        @Override
        public Expression parse(List<String> arguments) {
            // Ensure there are exactly three arguments
            if (arguments.size() != 3) {
                throw new IllegalArgumentException("Invalid number of arguments for SUB function. Expected 3, but got " + arguments.size());
            }

            // Parse the arguments
            Expression source = parseExpression(arguments.get(0).trim());
            Expression startIndex = parseExpression(arguments.get(1).trim());
            Expression endIndex = parseExpression(arguments.get(2).trim());

            // Validate that the source is a string and the indices are numeric or unknown
            CellType sourceCellType = source.getFunctionResultType();
            CellType startIndexCellType = startIndex.getFunctionResultType();
            CellType endIndexCellType = endIndex.getFunctionResultType();
            if (!sourceCellType.equals(CellType.STRING) ||
                    (!startIndexCellType.equals(CellType.NUMERIC) && !startIndexCellType.equals(CellType.UNKNOWN)) ||
                    (!endIndexCellType.equals(CellType.NUMERIC) && !endIndexCellType.equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for SUB function. Expected STRING and NUMERIC, but got " +
                        sourceCellType + ", " + startIndexCellType + ", and " + endIndexCellType);
            }

            // Create and return the SubExpression
            return new SubExpression(source, startIndex, endIndex);
        }
    },
    REF {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for REF function. Expected 1, but got " + arguments.size());
            }

            // verify indeed argument represents a reference to a cell and create a Coordinate instance. if not ok returns a null. need to verify it
            Coordinate target = CoordinateFactory.from(arguments.get(0).trim());
            if (target == null) {
                throw new IllegalArgumentException("Invalid argument for REF function. Expected a valid cell reference, but got " + arguments.get(0));
            }

            return new RefExpression(target);
        }
    }
    ;

    abstract public Expression parse(List<String> arguments);

    public static Expression parseExpression(String input) {

        if (input.startsWith("{") && input.endsWith("}")) {

            String functionContent = input.substring(1, input.length() - 1);
            List<String> topLevelParts = parseMainParts(functionContent);


            String functionName = topLevelParts.get(0).trim().toUpperCase();

            //remove the first element from the array
            topLevelParts.remove(0);
            // Validate function name against known enum constants
            try {
                return FunctionParser.valueOf(functionName).parse(topLevelParts);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error: The function name '" + functionName +
                        "' in your input '" + input + "' is either unknown or incorrectly used. " +
                        "Please ensure that you are using a valid function name and that you follow " +
                        "the correct syntax rules: {FUNCTION_NAME, arg1, arg2, ...}. Double-check your input and try again.");            }
        }

        // handle identity expression
        return FunctionParser.IDENTITY.parse(List.of(input.trim()));
    }

    private static List<String> parseMainParts(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '{') {
                stack.push(c);
            } else if (c == '}') {
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Error in function definition: It looks like there's an extra closing brace '}' at position " +
                            (i + 1) + " in your input: '" + input + "'. Please check your function and ensure that all opening '{' braces have matching closing '}' braces.");                }
                stack.pop();
            }

            if (c == ',' && stack.isEmpty()) {
                // If we are at a comma and the stack is empty, it's a separator for top-level parts
                parts.add(buffer.toString().trim());
                buffer.setLength(0); // Clear the buffer for the next part
            } else {
                buffer.append(c);
            }
        }

        // Add the last part
        if (buffer.length() > 0) {
            parts.add(buffer.toString().trim());
        }

        // Check for unbalanced opening braces
        if (!stack.isEmpty()) {
            throw new IllegalArgumentException("Error in function definition: Your input '" +
                    input + "' has unclosed opening braces '{'. Please make sure each '{' has a corresponding '}' to close it.");
        }

        return parts;
    }


    public static void main(String[] args) {
        String input = "{concat, hello,         world}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null);
        System.out.println("value: " + result.getValue() + " of type " + result.getCellType());
    }



}
