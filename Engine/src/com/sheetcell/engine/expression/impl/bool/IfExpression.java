package com.sheetcell.engine.expression.impl.bool;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.expression.api.TernaryExpression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class IfExpression implements TernaryExpression {
    private final Expression condition;
    private final Expression thenExpr;
    private final Expression elseExpr;

    public IfExpression(Expression condition, Expression thenExpr, Expression elseExpr) {
        this.condition = condition;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    @Override
    public Expression getFirst() {
        return condition;
    }

    @Override
    public Expression getSecond() {
        return thenExpr;
    }

    @Override
    public Expression getThird() {
        return elseExpr;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet, Cell callingCell) {
        // Evaluate the condition
        EffectiveValue conditionValue = condition.eval(sheet, callingCell);

        // Check for invalid condition
        if (Expression.isInvalidBoolean(conditionValue)) {
            return new EffectiveValue(CellType.BOOLEAN, "UNKNOWN");
        }

        Boolean conditionResult = conditionValue.castValueTo(Boolean.class);

        // If the condition result is null, treat it as an invalid expression
        if (conditionResult == null) {
            return new EffectiveValue(CellType.BOOLEAN, "UNKNOWN");
        }

        // Evaluate the appropriate branch based on the condition
        EffectiveValue resultValue = conditionResult ? thenExpr.eval(sheet, callingCell) : elseExpr.eval(sheet, callingCell);

        // Validate that both branches are of the same type
        EffectiveValue otherBranchValue = !conditionResult ? thenExpr.eval(sheet, callingCell) : elseExpr.eval(sheet, callingCell);
        if (resultValue.getCellType() != otherBranchValue.getCellType()) {
            throw new IllegalArgumentException("Error: The 'then' and 'else' expressions do not produce the same type.");
        }

        return resultValue;
    }

    @Override
    public CellType getFunctionResultType() {
        return thenExpr.getFunctionResultType();
    }

}

