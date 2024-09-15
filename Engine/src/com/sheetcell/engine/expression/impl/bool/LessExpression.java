package com.sheetcell.engine.expression.impl.bool;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.BinaryExpression;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class LessExpression implements BinaryExpression {
    private final Expression left;
    private final Expression right;

    public LessExpression(Expression left, Expression right) {
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

        // Check for invalid numeric values
        if (Expression.isInvalidNumeric(leftResult) || Expression.isInvalidNumeric(rightResult)) {
            return new EffectiveValue(CellType.BOOLEAN, "UNKNOWN");
        }

        Double leftValue = leftResult.castValueTo(Double.class);
        Double rightValue = rightResult.castValueTo(Double.class);

        if (leftValue == null || rightValue == null) {
            return new EffectiveValue(CellType.BOOLEAN, "UNKNOWN");
        }

        boolean result = leftValue <= rightValue;
        return new EffectiveValue(CellType.BOOLEAN, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }

}

