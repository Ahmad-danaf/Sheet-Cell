package com.sheetcell.engine.expression.impl.bool;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.BinaryExpression;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class EqualExpression implements BinaryExpression {
    private final Expression left;
    private final Expression right;

    public EqualExpression(Expression left, Expression right) {
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
        EffectiveValue leftResult = left.eval(sheet, callingCell);
        EffectiveValue rightResult = right.eval(sheet, callingCell);

        // Check for null or invalid values
        if (isInvalidValue(leftResult) || isInvalidValue(rightResult)) {
            return new EffectiveValue(CellType.BOOLEAN, "UNKNOWN"); // Return UNKNOWN for invalid inputs
        }

        // Check if both types are the same
        if (leftResult.getCellType() != rightResult.getCellType()) {
            return new EffectiveValue(CellType.BOOLEAN, Boolean.FALSE); // Different types can't be equal
        }

        boolean result = leftResult.getValue().equals(rightResult.getValue());
        return new EffectiveValue(CellType.BOOLEAN, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }

    private boolean isInvalidValue(EffectiveValue value) {
        return value == null || value.getValue() == null || value.getValue().toString().equals("UNKNOWN") ||
                value.getValue().toString().equals("NaN") || value.getValue().toString().equals("!UNDEFINED!");
    }
}
