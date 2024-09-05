package com.sheetcell.engine.expression.impl;

import com.sheetcell.engine.cell.Cell;
import com.sheetcell.engine.cell.CellType;
import com.sheetcell.engine.cell.EffectiveValue;
import com.sheetcell.engine.expression.api.Expression;
import com.sheetcell.engine.expression.api.TernaryExpression;
import com.sheetcell.engine.sheet.api.SheetReadActions;

public class SubExpression implements TernaryExpression {
    private final Expression source;
    private final Expression startIndex;
    private final Expression endIndex;

    public SubExpression(Expression source, Expression startIndex, Expression endIndex) {
        this.source = source;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public Expression getFirst() {
        return source;
    }

    @Override
    public Expression getSecond() {
        return startIndex;
    }

    @Override
    public Expression getThird() {
        return endIndex;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet, Cell callingCell) {
        // Evaluate the expressions
        EffectiveValue sourceValue = source.eval(sheet, callingCell);
        EffectiveValue startIndexValue = startIndex.eval(sheet, callingCell);
        EffectiveValue endIndexValue = endIndex.eval(sheet, callingCell);
        if (Expression.isInvalidString(sourceValue) || Expression.isInvalidNumeric(startIndexValue) || Expression.isInvalidNumeric(endIndexValue)) {
            return new EffectiveValue(CellType.STRING, "!UNDEFINED!");
        }

        // Convert the values to appropriate types
        String sourceStr = sourceValue.castValueTo(String.class);
        Double startDouble = startIndexValue.castValueTo(Double.class);
        Double endDouble = endIndexValue.castValueTo(Double.class);

        // Check for type conversion issues
        if (sourceStr == null || startDouble == null || endDouble == null) {
            return new EffectiveValue(CellType.STRING, "!UNDEFINED!"); // Return UNDEFINED if type conversion fails
        }


        // Convert indices to int
        int start = startDouble.intValue();
        int end = endDouble.intValue();

        // Check if indices are within bounds
        if (start < 0 || end >= sourceStr.length() || start > end) {
            return new EffectiveValue(CellType.STRING, "!UNDEFINED!"); // Return UNDEFINED if indices are invalid
        }

        // Extract the substring
        String result = sourceStr.substring(start, end + 1);

        // Return the result as a new EffectiveValue
        return new EffectiveValue(CellType.STRING, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.STRING;
    }
}

