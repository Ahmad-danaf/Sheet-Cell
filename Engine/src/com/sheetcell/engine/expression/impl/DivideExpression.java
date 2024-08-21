package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class DivideExpression implements Expression {

    private final Expression left;
    private final Expression right;

    public DivideExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        // Evaluate the expressions
        EffectiveValue leftValue = left.eval(sheet);
        EffectiveValue rightValue = right.eval(sheet);

        // Attempt to cast the right value (divisor) to Double
        Double divisor = rightValue.castValueTo(Double.class);
        if (divisor == null) {
            throw new IllegalArgumentException("Error: The divisor provided to the DIVIDE function is not numeric. " +
                    "Please ensure that the divisor is a valid numeric value.");
        }

        // Check for division by zero
        if (divisor == 0.0) {
            return new EffectiveValue(CellType.NUMERIC, Double.NaN); // Return NaN for division by zero
        }

        // Attempt to cast the left value (dividend) to Double
        Double dividend = leftValue.castValueTo(Double.class);
        if (dividend == null) {
            throw new IllegalArgumentException("Error: The dividend provided to the DIVIDE function is not numeric. " +
                    "Please ensure that the dividend is a valid numeric value.");
        }

        // Perform the division
        double result = dividend / divisor;

        // Return the result as a new EffectiveValue
        return new EffectiveValue(CellType.NUMERIC, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
