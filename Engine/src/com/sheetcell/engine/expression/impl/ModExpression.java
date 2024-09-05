package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.BinaryExpression;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class ModExpression implements BinaryExpression {
    private final Expression left;
    private final Expression right;

    public ModExpression(Expression left, Expression right) {
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

        if (Expression.isInvalidNumeric(leftValue) || Expression.isInvalidNumeric(rightValue)) {
            return new EffectiveValue(CellType.NUMERIC, Double.NaN);
        }
        // Attempt to cast the right value (divisor) to Double
        Double divisor = rightValue.castValueTo(Double.class);
        if (divisor == null) {
            return new EffectiveValue(CellType.NUMERIC, Double.NaN);
        }

        // Check for division by zero
        if (divisor == 0.0) {
            return new EffectiveValue(CellType.NUMERIC, Double.NaN); // Return NaN for modulo by zero
        }

        // Attempt to cast the left value (dividend) to Double
        Double dividend = leftValue.castValueTo(Double.class);
        if (dividend == null) {
            return new EffectiveValue(CellType.NUMERIC, Double.NaN);
        }

        // Perform the modulo operation
        double result = dividend % divisor;

        // Adjust the result to have the same sign as the divisor
        if (result != 0 && ((result < 0 && divisor > 0) || (result > 0 && divisor < 0))) {
            result += divisor;
        }

        // Return the result as a new EffectiveValue
        return new EffectiveValue(CellType.NUMERIC, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }

    private void test() {
        System.out.println("ModExpression test");
        System.out.println("Test 1: 5 % 2 = " + new ModExpression(new IdentityExpression(5.0,CellType.NUMERIC), new IdentityExpression(2.0,CellType.NUMERIC)).eval(null, null).getValue());
        System.out.println("Test 2: 5 % 0 = " + new ModExpression(new IdentityExpression(5.0,CellType.NUMERIC), new IdentityExpression(0.0,CellType.NUMERIC)).eval(null, null).getValue());
        System.out.println("Test 3: 5 % 2.5 = " + new ModExpression(new IdentityExpression(5.0,CellType.NUMERIC), new IdentityExpression(2.5,CellType.NUMERIC)).eval(null, null).getValue());
        System.out.println("Test 4: 5 % (-2) = " + new ModExpression(new IdentityExpression(5.0,CellType.NUMERIC), new IdentityExpression(-2.0,CellType.NUMERIC)).eval(null, null).getValue());
        System.out.println("Test 5: 10 % (-3)= " + new ModExpression(new IdentityExpression(10.0,CellType.NUMERIC), new IdentityExpression(-3.0,CellType.NUMERIC)).eval(null, null).getValue());
        System.out.println("Test 6: 10 % (-5) = " + new ModExpression(new IdentityExpression(10.0,CellType.NUMERIC), new IdentityExpression((-5.0),CellType.NUMERIC)).eval(null, null).getValue());
        System.out.println("Test 7: -10 % 3 = " + new ModExpression(new IdentityExpression(-10.0,CellType.NUMERIC), new IdentityExpression(3.0,CellType.NUMERIC)).eval(null, null).getValue());
    }
}

