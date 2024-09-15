package com.sheetcell.engine.expression.parser;

import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.coordinate.Coordinate;
import com.sheetcell.engine.coordinate.CoordinateFactory;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.expression.impl.*;
import com.sheetcell.engine.expression.impl.bool.*;
import com.sheetcell.engine.expression.impl.numeric.*;
import com.sheetcell.engine.expression.impl.string.ConcatExpression;
import com.sheetcell.engine.expression.impl.string.SubExpression;

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
            String actualValue = arguments.get(0);
            if (isBoolean(actualValue)) {
                return new IdentityExpression(Boolean.parseBoolean(actualValue.trim()), CellType.BOOLEAN);
            } else if (isNumeric(actualValue)) {
                return new IdentityExpression(Double.parseDouble(actualValue.trim()), CellType.NUMERIC);
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
            Expression left = parseExpression(arguments.get(0));
            Expression right = parseExpression(arguments.get(1));

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
            Expression left = parseExpression(arguments.get(0));
            Expression right = parseExpression(arguments.get(1));

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
            Expression left = parseExpression(arguments.get(0));
            Expression right = parseExpression(arguments.get(1));

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
            Expression left = parseExpression(arguments.get(0));
            Expression right = parseExpression(arguments.get(1));

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
            Expression left = parseExpression(arguments.get(0));
            Expression right = parseExpression(arguments.get(1));

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
            Expression base = parseExpression(arguments.get(0));
            Expression exponent = parseExpression(arguments.get(1));

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
            Expression argument = parseExpression(arguments.get(0));

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
            Expression str1 = parseExpression(arguments.get(0));
            Expression str2 = parseExpression(arguments.get(1));

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
            Expression source = parseExpression(arguments.get(0));
            Expression startIndex = parseExpression(arguments.get(1));
            Expression endIndex = parseExpression(arguments.get(2));

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
            Coordinate target = CoordinateFactory.from(arguments.get(0).trim().toUpperCase());
            if (target == null) {
                throw new IllegalArgumentException("Invalid argument for REF function. Expected a valid cell reference, but got " + arguments.get(0));
            }

            return new RefExpression(target);
        }
    },
    SUM {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for SUM function. Expected 1, but got " + arguments.size());
            }
            return new SumExpression(arguments.get(0).trim());
        }
    },
    AVERAGE {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for AVERAGE function. Expected 1, but got " + arguments.size());
            }
            return new AverageExpression(arguments.get(0).trim());
        }
    },
    PERCENT {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for PERCENT function. Expected 2, but got " + arguments.size());
            }

            Expression part = parseExpression(arguments.get(0).trim());
            Expression whole = parseExpression(arguments.get(1).trim());

            return new PercentExpression(part, whole);
        }
    },
    EQUAL {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for EQUAL function. Expected 2, but got " + arguments.size());
            }
            return new EqualExpression(parseExpression(arguments.get(0)), parseExpression(arguments.get(1)));
        }
    },
    NOT {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for NOT function. Expected 1, but got " + arguments.size());
            }
            return new NotExpression(parseExpression(arguments.get(0)));
        }
    },
    BIGGER {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for BIGGER function. Expected 2, but got " + arguments.size());
            }
            return new BiggerExpression(parseExpression(arguments.get(0)), parseExpression(arguments.get(1)));
        }
    },
    LESS {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for LESS function. Expected 2, but got " + arguments.size());
            }
            return new LessExpression(parseExpression(arguments.get(0)), parseExpression(arguments.get(1)));
        }
    },
    AND {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for AND function. Expected 2, but got " + arguments.size());
            }
            return new AndExpression(parseExpression(arguments.get(0)), parseExpression(arguments.get(1)));
        }
    },
    OR {
        @Override
        public Expression parse(List<String> arguments) {
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for OR function. Expected 2, but got " + arguments.size());
            }
            return new OrExpression(parseExpression(arguments.get(0)), parseExpression(arguments.get(1)));
        }
    },
    IF {
        @Override
        public Expression parse(List<String> arguments) {
            // Ensure there are exactly three arguments
            if (arguments.size() != 3) {
                throw new IllegalArgumentException("Invalid number of arguments for IF function. Expected 3, but got " + arguments.size());
            }

            // Parse the arguments
            Expression condition = parseExpression(arguments.get(0));
            Expression thenExpr = parseExpression(arguments.get(1));
            Expression elseExpr = parseExpression(arguments.get(2));

            // All is good, create the IfExpression instance
            return new IfExpression(condition, thenExpr, elseExpr);
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
            FunctionParser functionParser;
            try {
                functionParser= FunctionParser.valueOf(functionName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error: The function name '" + functionName +
                        "' in your input '" + input + "' is unknown.\n" +
                        "Please check the function name and ensure it is one of the following: ");
            }
            // Now, try to parse the function
            return functionParser.parse(topLevelParts);

        }

        // handle identity expression
        return FunctionParser.IDENTITY.parse(List.of(input));
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
                    throw new IllegalArgumentException("Error in function definition: It looks like there's an extra closing brace '}'. " +
                            "Please check your function and ensure that all opening '{' braces have matching closing '}' braces.");                }
                stack.pop();
            }

            if (c == ',' && stack.isEmpty()) {
                // If we are at a comma and the stack is empty, it's a separator for top-level parts
                parts.add(buffer.toString());
                buffer.setLength(0); // Clear the buffer for the next part
            } else {
                buffer.append(c);
            }
        }

        // Add the last part
        if (buffer.length() > 0) {
            parts.add(buffer.toString());
        }

        // Check for unbalanced opening braces
        if (!stack.isEmpty()) {
            throw new IllegalArgumentException("Error in function definition: Your input has unclosed opening braces '{'. " +
                    "Please make sure each '{' has a corresponding '}' to close it.");
        }

        return parts;
    }


    public static void main(String[] args) {
        String input = "{concat, hello,         world}";
        Expression expression = FunctionParser.parseExpression(input);
        EffectiveValue result = expression.eval(null,null);
        System.out.println("value: " + result.getValue() + " of type " + result.getCellType());

        try{
            String input2 = "{times,2 3}";
            Expression expression2 = FunctionParser.parseExpression(input2);
            EffectiveValue result2 = expression2.eval(null,null);
            System.out.println("value: " + result2.getValue() + " of type " + result2.getCellType());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



}
