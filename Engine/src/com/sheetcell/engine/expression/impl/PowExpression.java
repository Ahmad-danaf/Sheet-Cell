package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class PowExpression implements Expression {
    private final Expression base;
    private final Expression exponent;

    public PowExpression(Expression base, Expression exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        // Evaluate the expressions
        EffectiveValue baseValue = base.eval(sheet);
        EffectiveValue exponentValue = exponent.eval(sheet);

        // Attempt to cast the base and exponent to Double
        Double baseNumeric = baseValue.castValueTo(Double.class);
        Double exponentNumeric = exponentValue.castValueTo(Double.class);

        // If either value is null, it means the cast failed (not a numeric type)
        if (baseNumeric == null || exponentNumeric == null) {
            throw new IllegalArgumentException("Error: Both the base and the exponent provided to the POW function must be numeric. " +
                    "Please ensure that both arguments are valid numeric values.");
        }

        // Perform the power operation
        double result = Math.pow(baseNumeric, exponentNumeric);

        // Return the result as a new EffectiveValue
        return new EffectiveValue(CellType.NUMERIC, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}

