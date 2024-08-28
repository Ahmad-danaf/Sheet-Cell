package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.expression.api.UnaryExpression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class AbsExpression implements UnaryExpression {
    private final Expression argument;

    public AbsExpression(Expression argument) {
        this.argument = argument;
    }

    @Override
    public Expression getArgument() {
        return argument;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet, Cell callingCell) {
        // Evaluate the expression
        EffectiveValue value = argument.eval(sheet, callingCell);

        // Check if the value can be cast to Double (i.e., it is numeric)
        Double numericValue = value.castValueTo(Double.class);

        // If the value is null, it means the cast failed (not a numeric type)
        if (numericValue == null) {
            String cellCoordinates = (callingCell != null && callingCell.getCoordinate() != null)
                    ? callingCell.getCoordinate().toString()
                    : "unknown";
            if (cellCoordinates.equals("unknown")) {
                throw new IllegalArgumentException("Error: The argument provided to the ABS function is not numeric.\n" +
                        "Please ensure that the argument is a valid numeric value.");
            }
            else{
                throw new IllegalArgumentException("Error: The argument provided to the ABS function is not numeric.\n" +
                        "Please ensure that the argument is a valid numeric value. Cell: " + cellCoordinates);
            }
        }

        // Compute the absolute value
        double result = Math.abs(numericValue);

        // Return the result as a new EffectiveValue
        return new EffectiveValue(CellType.NUMERIC, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
