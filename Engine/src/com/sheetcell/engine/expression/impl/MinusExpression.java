package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.BinaryExpression;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class MinusExpression implements BinaryExpression {

    private Expression left;
    private Expression right;

    public MinusExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Expression getLeft() {
        return left;
    }

    @Override
    public Expression getRight() {
        return right;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet, Cell callingCell) {
        // Evaluate the expressions
        EffectiveValue leftValue = left.eval(sheet, callingCell);
        EffectiveValue rightValue = right.eval(sheet, callingCell);

        // Attempt to cast the left and right values to Double
        Double leftNumeric = leftValue.castValueTo(Double.class);
        Double rightNumeric = rightValue.castValueTo(Double.class);

        // If either value is null, it means the cast failed (not a numeric type)
        if (leftNumeric == null || rightNumeric == null) {
            String cellCoordinates = (callingCell != null && callingCell.getCoordinate() != null)
                    ? callingCell.getCoordinate().toString()
                    : "unknown";
            if ("unknown".equals(cellCoordinates)) {
                throw new IllegalArgumentException("Error: The operands provided to the MINUS function are not numeric.\n" +
                        "Please ensure that both operands are valid numeric values.");
            } else {
                throw new IllegalArgumentException("Error: The operands provided to the MINUS function are not numeric.\n" +
                        "Please ensure that both operands are valid numeric values. Cell: " + cellCoordinates);
            }
        }

        // Perform the subtraction
        double result = leftNumeric - rightNumeric;

        // Return the result as a new EffectiveValue
        return new EffectiveValue(CellType.NUMERIC, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
